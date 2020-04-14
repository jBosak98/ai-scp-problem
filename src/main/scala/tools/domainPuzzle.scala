package tools

import model.{CSPModel, CSPProblem, QuizVariable}
import tools.sudokuTools._

object domainPuzzle {
  def calculateDomainOfVariableIndex[V <: QuizVariable](problem: CSPModel[V], index: Int) = {
    val variable = problem.variables(index).get

    problem.domains(index) = calculateDomainForVariable(
      problem.asInstanceOf[CSPModel[QuizVariable]],
      variable)
      .asInstanceOf[List[String]]
    problem.domains(index)
  }


  def calculateDomainForEachVariables[V <: QuizVariable](problem: CSPModel[V]) = {
    problem.variables.indices.foreach { i =>
      domainPuzzle.calculateDomainOfVariableIndex(problem, i)
    }

  }

  def getVariablesThatConflictWithIndexes(
                                           problem: CSPModel[QuizVariable],
                                           indexes: List[Int],
                                           getVertical: Option[Boolean]): List[QuizVariable] =
    indexes.flatMap { index =>
      getVariablesThatConflictWithIndex(problem, index, getVertical)
    }.distinct


  def getVariablesThatConflictWithIndex(problem: CSPModel[QuizVariable], index: Int, getVertical: Option[Boolean]) = {
    val indices = if (getVertical.isEmpty) {
      val indicesOfColumn = getIndicesOfColumn(getColumnNumber(index, problem.size).get, problem.size)
      val indicesOfRow = getIndicesOfRow(getRowNumber(index, problem.size).get, problem.size)
      (indicesOfColumn ++ indicesOfRow).distinct
    } else if (getVertical.get) {
      getIndicesOfColumn(getColumnNumber(index, problem.size).get, problem.size)
    } else {
      getIndicesOfRow(getRowNumber(index, problem.size).get, problem.size)
    }
    problem.variables.filter(_.isDefined).map(_.get).filter { variable =>
      getIndicesThatAreFilledByVariable(problem, variable).exists(i => indices.contains(i))
    }
  }

  def calculateDomainForVariable(problem: CSPModel[QuizVariable], variable: QuizVariable) = {
    val availableValues = problem.availableValues.filter(_.length == variable.size)
    val filledIndices = getIndicesThatAreFilledByVariable(problem, variable)
    val variablesToCheck = getVariablesThatConflictWithIndexes(problem, filledIndices, Option(!variable.isVertical))
    defineAvailableValues(problem, filledIndices, availableValues, variablesToCheck)
  }


  def defineAvailableValues(
                             problem: CSPModel[QuizVariable],
                             filledIndicesByVariable: List[Int],
                             availableValues: List[String],
                             variablesToCheck: List[QuizVariable]
                           ): List[String] =
    availableValues.map { availableValue =>
      val IsWordProperForVariable = !filledIndicesByVariable
        .indices
        .zip(availableValue)
        .exists { case (filledIndice: Int, char: Char) =>
          variablesToCheck.exists { variableToCheck =>
            val valueInCheckedVariable = valueOfVariableAtIndex(problem, variableToCheck, filledIndicesByVariable(filledIndice))
            valueInCheckedVariable.isDefined && valueInCheckedVariable.get != char
          }
        }

      if (IsWordProperForVariable) Option(availableValue) else Option.empty[String]
    }.filter(_.isDefined).map(_.get)


  def valueOfVariableAtIndex(problem: CSPModel[QuizVariable], variable: QuizVariable, index: Int) = {
    val indicesOfVariable = getIndicesThatAreFilledByVariable(problem, variable)
    val indexInsideVariable = indicesOfVariable.indexOf(index)

    if (indexInsideVariable == -1 || variable.value.getOrElse("") == "") Option.empty[Char]
    else Option(variable.value.get.charAt(indexInsideVariable))

  }

  def getWholeLineIndicesThatAreFilledByVariable(problem: CSPModel[QuizVariable], variable: QuizVariable) =
    if (variable.isVertical)
      getIndicesOfColumn(getColumnNumber(variable.index, problem.size).get, problem.size)
    else
      getIndicesOfRow(getRowNumber(variable.index, problem.size).get, problem.size)


  def getIndicesThatAreFilledByVariable(problem: CSPModel[QuizVariable], variable: QuizVariable) = {
    val longLine = getWholeLineIndicesThatAreFilledByVariable(problem, variable)
    val indexInLongLine = longLine.indexOf(variable.index)
    longLine.slice(indexInLongLine, indexInLongLine + variable.size).toList
  }
}
