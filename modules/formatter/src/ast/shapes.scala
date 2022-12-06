/* Copyright 2022 Disney Streaming
 *
 * Licensed under the Tomorrow Open Source Technology License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://disneystreaming.github.io/TOST-1.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package smithytranslate
package formatter
package ast

import ast.NodeValue.NodeObject
import ast.shapes.ShapeBody.ListStatement.ListMembers
import ast.shapes.ShapeBody.MapStatement.MapMembers
import ast.shapes.ShapeBody.StructureMembers.{
  StructureMember,
  StructureMemberType
}
import cats.data.NonEmptyList

object shapes {
  // UseSection and ShapeStatements are both Optional , however encoded via Lists . see https://github.com/awslabs/smithy/issues/1249
  case class ShapeSection(
      all: Option[
        (NamespaceStatement, UseSection, ShapeStatements)
      ]
  )

  case class NamespaceStatement(
      namespace: Namespace,
      break: Break
  )

  case class UseSection(uses: List[UseStatement])

  case class UseStatement(
      absoluteRootShapeId: AbsoluteRootShapeId,
      break: Break
  )

  case class ShapeStatements(statements: List[ShapeStatementsCase])

  sealed trait ShapeStatementsCase

  object ShapeStatementsCase {
    case class ShapeStatementCase(statement: ShapeStatement)
        extends ShapeStatementsCase

    case class ApplyStatementCase(apply: ApplyStatement)
        extends ShapeStatementsCase
  }

  case class ShapeStatement(
      traitStatements: TraitStatements,
      shapeBody: ShapeBody,
      br: Break
  )

  /*  case class ShapeMemberKvp(
      traitStatements: TraitStatements,
      identifier: Identifier,
      ws0: Whitespace,
      ws1: Whitespace,
      shapeId: ShapeId
  )

  sealed trait ShapeMembers

  object ShapeMembers {
    case class Empty(ws: Whitespace) extends ShapeMembers

    case class Populated(
        ws0: Whitespace,
        shapeMemberKvp: ShapeMemberKvp,
        ws1: Whitespace,
        additionalMembers: List[(Comma, ShapeMemberKvp, Whitespace)]
    ) extends ShapeMembers
  }*/

  sealed trait ShapeBody

  object ShapeBody {
    case class SimpleTypeName(name: String) extends AnyVal

    case class SimpleShapeStatement(
        decl: SimpleTypeName,
        identifier: Identifier,
        mixins: Option[Mixin]
    ) extends ShapeBody

    case class EnumShapeStatement(
        typeName: String,
        id: Identifier,
        mixin: Option[Mixin],
        whitespace: Whitespaces,
        enumShapeMembers: EnumShapeMembers
    ) extends ShapeBody

    case class ListStatement(
        identifier: Identifier,
        mixin: Option[Mixin],
        whitespace: Whitespaces,
        members: ListMembers
    ) extends ShapeBody

    object ListStatement {
      sealed trait ListMemberType

      case class ElidedListMember(member: ShapeIdMember) extends ListMemberType

      case class ExplicitListMember(shapeId: ShapeId) extends ListMemberType

      case class ListMembers(
          ws0: Whitespaces,
          members: ListMember,
          ws1: Whitespaces
      )

      case class ListMember(
          traitStatements: TraitStatements,
          listMemberType: ListMemberType
      )
    }

    case class MapStatement(
        identifier: Identifier,
        mixin: Option[Mixin],
        whitespace: Whitespaces,
        members: MapMembers
    ) extends ShapeBody

    object MapStatement {
      case class MapKey(
          traitStatements: Option[TraitStatements],
          mapKeyType: MapKeyType
      )

      sealed trait MapKeyType

      object MapKeyType {
        case class ElidedMapKey(name: ShapeIdMember) extends MapKeyType

        case class ExplicitMapKey(shapeId: ShapeId) extends MapKeyType
      }

      case class MapValue(
          traitStatements: Option[TraitStatements],
          mapValueType: MapValueType
      )

      sealed trait MapValueType

      object MapValueType {
        case class ElidedMapValue(member: ShapeIdMember) extends MapValueType

        case class ExplicitMapValue(shapeId: ShapeId) extends MapValueType
      }

      case class MapMembers(
          ws0: Whitespaces,
          mapKey: MapKey,
          whitespace: Whitespace,
          mapValue: MapValue,
          ws1: Whitespaces
      )
    }

    case class StructureStatement(
        identifier: Identifier,
        resource: Option[StructureResource],
        mixins: Option[Mixin],
        whitespace: Whitespaces,
        members: StructureMembers
    ) extends ShapeBody

    case class StructureResource(shapeId: ShapeId)

    case class StructureMembers(
        ws0: Whitespaces,
        members: List[(TraitStatements, StructureMember, Whitespaces)]
    )

    object StructureMembers {
      sealed trait StructureMemberType

      object StructureMemberType {
        case class ElidedStructureMember(identifier: Identifier)
            extends StructureMemberType

        case class ExplicitStructureMember(
            identifier: Identifier,
            shapeId: ShapeId
        ) extends StructureMemberType
      }

      case class StructureMember(
          structureMemberType: StructureMemberType,
          valueAssignment: Option[ValueAssignment]
      )
    }

    case class UnionStatement(
        identifier: Identifier,
        mixin: Option[Mixin],
        whitespace: Whitespaces,
        members: UnionMembers
    ) extends ShapeBody

    case class UnionMembers(
        whitespace: Whitespaces,
        members: List[(TraitStatements, UnionMember, Whitespaces)]
    )

    case class UnionMember(structureMemberType: StructureMemberType)

    case class ServiceStatement(
        identifier: Identifier,
        mixin: Option[Mixin],
        whitespace1: Whitespaces,
        nodeObject: NodeObject
    ) extends ShapeBody

    case class ResourceStatement(
        identifier: Identifier,
        mixin: Option[Mixin],
        whitespace: Whitespaces,
        nodeObject: NodeObject
    ) extends ShapeBody

    case class InlineStructure(
        whitespace: Whitespaces,
        traitStatements: TraitStatements,
        mixin: Option[Mixin],
        ws1: Whitespaces,
        members: StructureMembers
    )

    case class OperationBody(
        whitespace: Whitespaces,
        input: Option[OperationInput],
        output: Option[OperationOutput],
        errors: Option[OperationErrors],
        ws1: Whitespaces
    )

    case class OperationInput(
        whitespace: Whitespaces,
        either: Either[InlineStructure, (Whitespaces, ShapeId)],
        whitespace1: Whitespace
    )

    case class OperationOutput(
        whitespace: Whitespaces,
        either: Either[InlineStructure, (Whitespaces, ShapeId)],
        whitespace1: Whitespace
    )

    case class OperationErrors(
        ws0: Whitespaces,
        ws1: Whitespaces,
        list: List[(Whitespaces, Identifier)],
        ws2: Whitespaces,
        ws3: Whitespace
    )

    case class OperationStatement(
        identifier: Identifier,
        mixin: Option[Mixin],
        whitespace: Whitespaces,
        operationBody: OperationBody
    ) extends ShapeBody

    case class Mixin(
        whitespace: Whitespaces,
        shapeIds: NonEmptyList[(Whitespaces, ShapeId)],
        whitespace1: Whitespaces
    )

    case class EnumShapeMembers(
        whitespace: Whitespaces,
        members: NonEmptyList[
          (TraitStatements, Identifier, Option[ValueAssignment], Whitespaces)
        ]
    )

    case class ValueAssignment(value: NodeValue, break: Break)
  }
}
