package utils;

import java.io.File

fun relativePath(p1: String, p2: String): String {
  return p1.split("/").dropLast(1).joinToString("/") + "/" + p2;
}

fun changeExtension(file: String, extension: String): String {
  return file.replaceAfterLast(".", extension)
}

fun changeFileName(file: String, name: String): String{
  return file.replaceAfterLast("/", name) + "." + file.substringAfterLast(".")
}

fun getFileName(file: String): String {
  return File(file).nameWithoutExtension
}

fun filePathInDir(file: String, dir: String): String {
  return dir + "/" + file;
}