# Project Hapi

## <ins>H​</ins>ierarchical <ins>A</ins>ccess <ins>P</ins>olicy <ins>I</ins>mplementation

This project aims to implement a domain specific language to define access policies. Considering actors (A) and resources (R), we want to be able to ensure:
 - **Totality**: either A accesses R or it does not
 - **Uniqueness**: R cannot be both accessible and forbidden to A
 
By hierarchical, we mean that policies specified for an actor can be projected into another actor, and extended in this new setting.

## Usage

Suppose you have a Hapi project under the folder `<project_dir>` and then you want to run the file `<project_dir>/<file>.hp`:

 - **With Docker**

```
docker build -t hapi_image .
docker run -v <project_dir>:/data --rm hapi_image "/data/<file>.hp"
```

 - **Without Docker**

```
gradle run --args="<project_dir>/<file>.hp"
```

In both ways the YAML translation will be generated at `<project_dir>/<file>.yaml`.

## Examples

@TODO

## License

This project is licensed under the [GPLv3 license](LICENSE).