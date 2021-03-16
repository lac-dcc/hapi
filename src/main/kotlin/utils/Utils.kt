package utils

import java.io.File

fun changeExtension(file: String, extension: String): String {
  return file.replaceAfterLast(".", extension)
}

fun changeFileName(file: String, name: String): String{
  return file.replaceAfterLast("/", name) + "." + file.substringAfterLast(".")
}

fun getFileName(file: String): String {
  return File(file).nameWithoutExtension
}

fun getDirName(file: String): String {
  return file.split("/").dropLast(1).joinToString("/")
}

fun relativePath(p1: String, p2: String): String {
  return getDirName(p1) + "/" + p2;
}

fun filePathInDir(file: String, dir: String): String {
  return dir + "/" + file;
}