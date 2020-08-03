# react-native-q2-printer

## Getting started

`$ npm install react-native-q2-printer --save`

### Mostly automatic installation

`$ react-native link react-native-q2-printer`

## Usage
```javascript
import CBQ2Printer from 'react-native-q2-printer';

// TODO: What to do with the module?
CBQ2Printer;
```
### Methods
```javascript
 
   CBQ2Printer.init();
    let logo="";                  
    CBQ2Printer.addImage({img:logo,size:8,align:1});
    CBQ2Printer.addFormatText('afdsafsafas',24,1);
    CBQ2Printer.addBlankLine(1,6);
    CBQ2Printer.addTable({rows:[['column1','1'],['column2','2'],['column3','3']]},[20,6],[0,2],24,1);
    CBQ2Printer.addBlankLine(1,6);
    CBQ2Printer.addBarCode('7687568657',8,6,12,1);
    CBQ2Printer.addQRCode('afdsafsafas',1,3);

    CBQ2Printer.print();
    ```
