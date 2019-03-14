# Artifact for Code Completion Benchmark -- ICSE 2019
Here we release the benchmark data for the experiments detailed in our [ICSE 2019 paper](http://vhellendoorn.github.io/PDF/icse2019.pdf): When Code Completion Fails: a Case Study on Real-World Completions. This repository primarily releases our benchmark data to facilitate comparing future completion tools' performance on more realistic data in general, and against the models reported in our paper in specific. For those interested in replicating our models from scratch, we briefly describe the modeling setup as well. See our paper for more details.

Before reading on, it is important that we stress (as we do in the paper) the scope of this data: its goal was strictly to understand the challenges facing code completion tools on real-world data, not to quantify their true real-world accuracy. This dataset is derived from prior work and includes only Visual Studio completions that were accepted by developers in C# development. It did not include any information regarding completions that were canceled (and these may well be very important depending on your tool's purpose). This was appropriate for us to highlight areas where improvement is necessary in existing tools. However, if you are developing a code completion tool and wish to assess its true, real-world accuracy, this dataset can only give you a rudimentary understanding of its efficacy beyond synthesized benchmarks; to truly assess real-world efficacy, our field greatly needs novel benchmarks from developer studies. If you are interested in this, please feel free to reach out to us for advice on setting up such studies; our paper includes several guidelines as well.

## Data
Our work is based on publicly available datasets; we pre-processed these and release the processed data here to facilitate replication and future benchmarking. This section describes the various datasets involved; the main data resource is described under "Benchmarking data" and includes all accessible context and meta-data for each benchmarked completion.

### Data origin
We make use of two pre-existing datasets in our work, both of which are part of the [Kave project](http://www.kave.cc/) (not a contribution of our work). The training data consists of 309 C# repositories (see [here](http://www.kave.cc/datasets) -> Static Repository Data, version: Contexts (May 3, 2017)); the benchmark data consists of a subset of the code completion Events (described below) from the MSR challenge data (same page, Interaction Data, version: Events (Mar 1, 2017)). In addition, the dataset comes with an open-source repository ([link](https://github.com/stg-tud/kave-java)) that includes several tools to process the SST data.

### Our contributions
__Token representation:__ The public data is stored in the form of Simplified Syntax Trees (SST, much like ASTs with type anntoations). We produce a textual representation to be used by models that do not act on syntax trees (e.g. n-gram models, standard RNNs). To do so, we made some minor extensions to the SSTPrintingVisitor included in the aforementioned repository (especially to re-inline repeated invocations; see our paper) and print each file as tab-separated tokens on a single line. All our plain-text files are constructed this way, both for training and benchmarking data. For reference, we also release the two files that we modified from the original reposistory in Code/Printing to create this data, though we stress that these are individual Java files that will not function without the aforementioned public repository context and may not be up-to-date with the original code.

__Completion Selection:__ The public dataset contained many types of IDE interaction events and many completion events that were canceled or filtered out (and thus did not have the correct completion available). We extracted only the subset of these that allows us to benchmark our tools. Our paper details our selection criteria; the end-result is 15,247 completion events, each with their accepted completion and context. Note that the paper mentions 15,245 completion events: two completions were extracted but caused run-time errors for our main (n-gram) model. These are nonetheless included as they may benefited benchmarking of other tools.

### Training data
The SSTs (syntax trees) used as training data can be downloaded from the Kave website using the aforementioned link; the plain-text data is included in Data/Training.7z -- we compressed all our data with 7-zip as it produced substantially smaller files than plain "zip". Note that there is also a 10% subset of this training data; its purpose is explained under Models --> Datasets below. This data is formatted as stated above (tab-separated tokens on a single line) and otherwise preserves the file structure of the public dataset.

### Benchmarking data
Our selected completion events are available in Data/SST-Completions.7z (for SST data) and Data/Plaintext-Completions.7z (for tab-separated plain-text tokens). The completions are grouped by developer (identified just by their ID) and by the index of the event in that developer's overall event-stream.

The SST files simply contain the JSON-formatted syntax tree pertaining to each completion context, with a placeholder at the completion site (specifically in the form of a node with type "Expressions.Assignable.CompletionExpression"). Our BMN+ model uses this format and dataset.

The plain-text files contain some additional context, including the correct completion. There are some important differences compared to the above data:
first, each completion directory in this dataset contains a "Content" directory in which we store a reconstruction of all the files of which we know the content at completion time (not just the file to be completed). This was necessary to benchmark our nested-cache n-gram model (one of the n-gram models we tested), which relies on information in surrounding files to make its predictions; it is an incomplete representation of the development context (that gets more accurate over time) because the event dataset does not contain the full state of the project at each completion.
Secondly, each completion directory contains a "data" file that describes the meta-data pertaining to this completion. This is a simple new-line separated file that contains the following data:

- File (under "Content") in which completion took place
- Index of location of completion (0-based, no placeholder included)
- Prefix, if any (tokens typed to narrow down the correct completion, not case-sensitive)
- Completion categorization (e.g. "EXTERNAL_API", see paper)
- Completion resolution (should always be "Applied"; original data also contained "Canceled" and "Filtered")
- Correct completion: tab-separated, first the simple name (which we aim to predict), and then the fully qualified version (which includes type information)
- Completion duration (in milliseconds)
- Number of selections (#no-selections) before choosing completion
- A list of #no-selections selections, each prefixed with the time since the last selection (or since the start). Note that the same item may be included several times.
- The number of completion options (#no-options) that Visual Studio presented to the developer
- The list of #no-options completion candidates that were presented to the developer, in the order in which they were presented (typically alphabetically sorted)

This data should allow replication of our models' performance and benchmarking of most other tools.

## Models
The main purpose of this artifact is to facilitate evaluating of other code completion tools and comparing with the evaluation results that are published in detail in our paper. For those interested in replicating our models, we currently include a brief description of the models we used here (we may post precise replication scripts later). Our n-gram and RNN models used off-the-shelf implementations (not contributions of our work) that should be straightforward to apply to our pre-processed dataset. We also evaluate an extension of the Best-Matching Neighbor (BMN) model, called BMN+, which was specifically enhanced and extended for C#. This is a contribution of our work and details on how to run this model will be included soon.

### Datasets
Our neural model was not able to model the full training data due to its very large vocabulary and long training duration (a known problem with RNNs for code). We randomly sampled 10% of the projects for training instead. This training data is stored as a single (large) file, with each original C# file printed on one line (tokens again tab-separated), available in Data/Training-10%.7z. Our n-gram model was trained with this same dataset for fairness (though we also studied training it with the full dataset); the BMN+ model was not constrained this way because it only acts on a subset of completions.

### n-gram Models
We relied on the [SLP-Core toolkit](https://github.com/SLP-team/SLP-Core/) to train and test our n-gram models. Since the data is already tokenized, we simply use their TokenizedLexer and raised the prediction limit to 1,000 (from the default 10), otherwise keeping the settings at their defaults (e.g. 6-gram models, Jelinek-Mercer smoothing, open vocabularies at test time). The models are trained on the aformentioned plain-text training data and tested on each completion file (possibly with caches and/or nested context; we included several types of n-gram models). We write out all predictions at each location using the tools "verbose" mode and simply extracted the prediction list at the appropriate index after-the-fact.

### Neural model
Our neural model follows a similar pattern to the above: we use an off-the-shelf RNN implementation from CNTK (other toolkits should produce equivalent results), adjusting the hyper-parameters only slightly as detailed in the paper, pre-train it on the tab-separated tokens in the training data and evaluate it on each completion file (specifically extracting top_k predictions for k=1,000), retrieving the appropriate predictions at the completion index after the fact. We also include a dynamic version, which is trained on each file after predicting the completion.

### BMN+
Model description to follow.

