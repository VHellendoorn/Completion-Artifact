package data.loader.sst;

/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cc.kave.commons.model.naming.codeelements.IParameterName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.model.naming.types.ITypeParameterName;
import cc.kave.commons.model.naming.types.organization.INamespaceName;
import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.visitor.ISSTNodeVisitor;
import cc.kave.commons.model.typeshapes.ITypeShape;

public class SSTPrintingContextExtended {

	// TODO: change the comments in this file to JavaDoc format

	/// <summary>
	/// Base indentation level to use while printing SST nodes.
	/// </summary>
	public int indentationLevel;

	/// <summary>
	/// Type shape (supertype information) of the SST. If the SST is a type and
	/// a type shape is
	/// provided, the supertypes will be included :the print result.
	/// </summary>
	public ITypeShape typeShape;

	/// <summary>
	/// Collection of namespaces that have been seen by the context while
	/// processing an SST.
	/// </summary>
	public Iterator<INamespaceName> SeenNamespaces;

	private StringBuilder _sb;
	private Set<INamespaceName> _seenNamespaces;

	public SSTPrintingContextExtended() {
			_sb = new StringBuilder();
			_seenNamespaces = new HashSet<INamespaceName>();
		}

	/// <summary>
	/// appends a String to the context.
	/// </summary>
	/// <param name="text">The String to append.</param>
	/// <returns>The context after appending.</returns>
	public SSTPrintingContextExtended text(String text) {
		_sb.append("\t" + text.trim());
		return this;
	}

	public int getIndentationLevel() {
		return indentationLevel;
	}

	public void setIndentationLevel(int indentationLevel) {
		this.indentationLevel = indentationLevel;
	}

	public ITypeShape getTypeShape() {
		return typeShape;
	}

	public void setTypeShape(ITypeShape typeShape) {
		this.typeShape = typeShape;
	}

	public Iterator<INamespaceName> getSeenNamespaces() {
		return _seenNamespaces.iterator();
	}

	public void setSeenNamespaces(Iterator<INamespaceName> seenNamespaces) {
		SeenNamespaces = seenNamespaces;
	}

	/// <summary>
	/// appends a comment to the context. Delimiters must be provided.
	/// </summary>
	/// <param name="commentText">The comment to append.</param>
	/// <returns>The context after appending.</returns>
	public SSTPrintingContextExtended comment(String commentText) {
		return text(commentText);
	}

	/// <summary>
	/// appends a line break to the context.
	/// </summary>
	/// <returns>The context after appending.</returns>
	public SSTPrintingContextExtended newLine() {
		_sb.append("\n");
		return this;
	}

	/// <summary>
	/// appends a space to the context.
	/// </summary>
	/// <returns>The context after appending.</returns>
	public SSTPrintingContextExtended space() {
		_sb.append("\t");
		return this;
	}

	/// <summary>
	/// appends the appropriate amount of indentation to the context according
	/// to the current indentation level. Should
	/// always be used after appending a line break.
	/// </summary>
	/// <returns>The context after appending.</returns>
	public SSTPrintingContextExtended indentation() {
		for (int i = 0; i < indentationLevel; i++) {
			_sb.append("\t");
		}
		return this;
	}

	/// <summary>
	/// appends a keyword (e.g. "null", "class", "static") to the context.
	/// </summary>
	/// <param name="keyword">The keyword to append.</param>
	/// <returns>The context after appending.</returns>
	public SSTPrintingContextExtended keyword(String keyword) {
		return text(keyword);
	}

	/// <summary>
	/// appends a marker for the current cursor position to the context.
	/// </summary>
	/// <returns>The context after appending.</returns>
	public SSTPrintingContextExtended cursorPosition() {
		// Note: no tab before cursor!
		_sb.append("!!");
		return this;
	}

	/// <summary>
	/// appends a marker for an unknown entity to the context.
	/// </summary>
	/// <returns>The context after appending.</returns>
	public SSTPrintingContextExtended unknownMarker() {
		return text("???");
	}

	/// <summary>
	/// appends a left angle bracket ("<![CDATA[<]]>") to the context.
	/// </summary>
	/// <returns>The context after appending.</returns>
	public SSTPrintingContextExtended leftAngleBracket() {
		return text("<");
	}

	/// <summary>
	/// appends a right angle bracket ("("<![CDATA[>]]>") to the context.
	/// </summary>
	/// <returns>The context after appending.</returns>
	public SSTPrintingContextExtended rightAngleBracket() {
		return text(">");
	}

	/// <summary>
	/// appends a String literal to the context. Quotation marks must not be
	/// provided.
	/// </summary>
	/// <param name="value">The String to append.</param>
	/// <returns>The context after appending.</returns>
	public SSTPrintingContextExtended stringLiteral(String value) {
		return text("\"str\"");
	}

	/// <summary>
	/// appends the name (and only the name!) of a type to the context.
	/// </summary>
	/// <param name="typeName">The type name to append.</param>
	/// <returns>The context after appending.</returns>
	public SSTPrintingContextExtended typeNameOnly(ITypeName typeName) {
		if (typeName != null)
			return text(typeName.getName());
		return this;
	}

	protected SSTPrintingContextExtended typeParameterShortName(String typeParameterShortName) {
		return text(typeParameterShortName);
	}

	/// <summary>
	/// Formats and appends a type name together with its generic types to the
	/// context.
	/// </summary>
	/// <param name="typeName">The type name to append.</param>
	/// <returns>The context after appending.</returns>
	public SSTPrintingContextExtended type(ITypeName typeName) {

		typeNameOnly(typeName);

		if (typeName != null && typeName.hasTypeParameters()) {
			typeParameters(typeName.getTypeParameters());
		}

		return this;
	}

	public SSTPrintingContextExtended typeParameters(List<ITypeParameterName> tpns) {
		leftAngleBracket();

		boolean isFirst = true;
		for (ITypeParameterName tpn : tpns) {

			if (!isFirst) {
				_sb.append("\t,");
			}
			isFirst = false;

			if (tpn.isUnknown()) {
				typeParameterShortName("?");
			} else if (tpn.isBound()) {
				type(tpn.getTypeParameterType());
			} else {
				_sb.append("\t" + tpn.getTypeParameterShortName());
			}
		}

		rightAngleBracket();

		return this;
	}

	/// <summary>
	/// Formats and appends a parameter list to the context.
	/// </summary>
	/// <param name="parameters">The list of parameters to append.</param>
	/// <returns>The context after appending.</returns>
	public SSTPrintingContextExtended parameterList(List<IParameterName> list) {
		text("(");

		for (IParameterName parameter : list) {
			if (parameter.isPassedByReference() && parameter.getValueType().isValueType()) {
				keyword("ref").space();
			}

			if (parameter.isOutput()) {
				keyword("out").space();
			}

			if (parameter.isOptional()) {
				keyword("opt").space();
			}

			if (parameter.isParameterArray()) {
				keyword("params").space();
			}

			type(parameter.getValueType()).space().text(parameter.getName());

			if (!parameter.equals(list.get(list.size() - 1))) {
				text(",").space();
			}
		}

		text(")");

		return this;
	}

	/// <summary>
	/// appends a statement block to the context with correct indentation.
	/// </summary>
	/// <param name="block">The block to append.</param>
	/// <param name="visitor">The visitor to use for printing each
	/// statement.</param>
	/// <param name="withBrackets">If false, opening and closing brackets will
	/// be omitted.</param>
	public SSTPrintingContextExtended statementBlock(List<IStatement> block, ISSTNodeVisitor<SSTPrintingContextExtended, Void> visitor,
			boolean withBrackets) {
		if (block.isEmpty()) {
			if (withBrackets) {
				text("\t{\t}");
			}

			return this;
		}

		if (withBrackets) {
			newLine().indentation().text("{");
		}

		indentationLevel++;

		for (IStatement statement : block) {
			newLine();
			statement.accept(visitor, this);
		}

		indentationLevel--;

		if (withBrackets) {
			newLine().indentation().text("}");
		}

		return this;
	}

	public String toString() {
		return _sb.toString();
	}
}