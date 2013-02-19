package com.jetbrains.python.psi.search;

import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author yole
 */
public class PySuperMethodsSearchExecutor implements QueryExecutor<PsiElement, PySuperMethodsSearch.SearchParameters> {
  public boolean execute(@NotNull final PySuperMethodsSearch.SearchParameters queryParameters, @NotNull final Processor<PsiElement> consumer) {
    PyFunction func = queryParameters.getDerivedMethod();
    String name = func.getName();
    PyClass containingClass = func.getContainingClass();
    Set<PyClass> foundMethodContainingClasses = new HashSet<PyClass>();
    if (name != null && containingClass != null) {
      for (PyClass superClass : containingClass.iterateAncestorClasses()) {
        if (!queryParameters.isDeepSearch()) {
          boolean isAlreadyFound = false;
          for (PyClass alreadyFound : foundMethodContainingClasses) {
            if (alreadyFound.isSubclass(superClass)) {
              isAlreadyFound = true;
            }
          }
          if (isAlreadyFound) {
            continue;
          }
        }
        PyFunction superMethod = superClass.findMethodByName(name, false);
        if (superMethod != null) {
          final Property property = func.getProperty();
          final Property superProperty = superMethod.getProperty();
          if (property != null && superProperty != null) {
            final AccessDirection direction = PyUtil.getPropertyAccessDirection(func);
            final Callable callable = superProperty.getByDirection(direction).valueOrNull();
            superMethod = (callable instanceof PyFunction) ? (PyFunction)callable : null;
          }
        }
        if (superMethod != null) {
          foundMethodContainingClasses.add(superClass);
          if (!consumer.process(superMethod)) {
            return false;
          }
        }
      }
    }
    return true;
  }
}
