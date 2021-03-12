

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;


public class SicAssembler {
    String y = "";
    String z = "";
    String a = "";
    int adduni=0;
    String pc="";
    //String b= "";
    String end = "";
    String org = null;
    String base=null;
    Hashtable<String, String> symtab = new Hashtable<>();
    ArrayList<InstructionSet> instructionset;
    ArrayList<AssemblyCode> code;


    public void ReadAssemblyCode() throws FileNotFoundException {
        code = new ArrayList<>();

        File f = new File("in.txt");
        Scanner scan = new Scanner(f);
        while (scan.hasNextLine()) {
            String l = scan.nextLine();
            String[] inst = l.split("\\s+");

            if (inst.length == 3) {
                String label = inst[0];
                String instruction = inst[1];
                String value = inst[2];
                code.add(new AssemblyCode(label, instruction, value, "", ""));


            } else if (inst.length == 2) {
                String label = "-----";
                String instruction = inst[0];
                String value = inst[1];
                code.add(new AssemblyCode(label, instruction, value, "", ""));

            } else {
                String label = ".";
                String instruction = "none";
                String value = "0";


            }

        }

    }

    public void PassOne() throws IOException {


        String startingAddress = code.get(0).getValue();

        code.get(0).setAdress(startingAddress);
        int calculator = Integer.parseInt(startingAddress, 16);
        int counter = 0;
        symtab.put(code.get(0).getLabel(), startingAddress);
        for (int i = 1; i < code.size(); i++) {
            calculator += counter;
            String addressInHex = Integer.toHexString(calculator);
            addressInHex = Refill(addressInHex, 4);
            code.get(i).setAdress(addressInHex);


            String value = code.get(i).getValue();
            String inst = code.get(i).getInstruction();
            String comment = code.get(i).getLabel();


            switch (code.get(i).getInstruction()) {


                case "RESB" -> {
                    counter = Integer.parseInt(value);
                    break;
                }

                case "END" -> {
                    counter = 0;
                    break;
                }

                case "BASE" -> {
                    counter = 0;
                    break;
                }

                case "EQU" -> {
                    counter = 0;
                    break;
                }

                case "LTRORG" -> {
                    counter = 0;
                    break;
                }

                case "RESW" -> {
                    int v = Integer.parseInt(value);
                    counter = v * 3;
                    break;
                }


                case "RESDW" -> {
                    int v = Integer.parseInt(value);
                    counter = v * 6;
                    break;
                }

                case ("FIX"), ("SIO"), ("FLOAT"), ("HIO"), ("TIO") -> {

                    counter = 1;
                    break;
                }

                case ("DIVR"), ("CLEAR"), ("COMPR"), ("ADDR"), ("MULR"), ("RMO"), ("SHIFTR"), ("SUBR"), ("SVC"), ("TIXR"), ("SHIFTL") -> {

                    counter = 2;
                    break;
                }


                case "BYTE" -> {
                    if (value.startsWith("C")) {
                        counter = value.length() - 3;
                        break;
                    } else
                        counter = 1;

                }


                default -> counter = 3;
            }
            if (inst.startsWith("+")) {
                counter = 4;
            } else if (inst.startsWith("$")) {
                counter = 5;
            } else if (comment.startsWith(".")) {
                counter = 0;
            }

            if (!code.get(i).getInstruction().equals("END") &&
                    !code.get(i).getInstruction().equals("BASE") && !code.get(i).getInstruction().equals("LTRORG") &&
                    !code.get(i).getLabel().equals("*") && !code.get(i).getInstruction().equals("START") &&
                    !code.get(i).getInstruction().equals("EQU") &&
                    !code.get(i).getLabel().equals(".")) {
                symtab.put(code.get(i).getLabel(), addressInHex);

            }
        }
        System.out.println(symtab);
        File f = new File("SymbolTable.txt");
        FileWriter w = new FileWriter(f);
        w.write(String.valueOf(symtab));
        w.write(System.lineSeparator());
        w.close();

        for (AssemblyCode x : code) {


            if (x.getLabel().equals(".")) {
                System.out.println(" ");
            } else if (x.getInstruction().equals("END") || x.getInstruction().equals("BASE") || x.getLabel().equals("LTRORG")
            )
                System.out.println("-----" + "\t" + x.getLabel() + "\t" + x.getInstruction() + "\t");


            else
                System.out.println(x.getAdress() + "\t" + x.getLabel() + "\t" + x.getInstruction() + "\t" + x.getValue());
        }
    }


    public String Refill(String s, int length) {
        StringBuilder sBuilder = new StringBuilder(s);
        while (sBuilder.length() < length) {
            sBuilder.insert(0, "0");
        }
        s = sBuilder.toString();
        return s;
    }

    public void ScanInstructionSet() throws FileNotFoundException {

        File f = new File("Instruction set.txt");
        Scanner scan = new Scanner(f);
        instructionset = new ArrayList<>();
        while (scan.hasNext()) {
            String insName = scan.next();
            String insOpcode = scan.next();
            instructionset.add(new InstructionSet(insName, insOpcode));


        }
        //System.out.println("INSTRUCTIOS");
        int flag = 0;
        for (AssemblyCode x : code) {
            for (int i = 0; i < 73; i++) {
                //System.out.println(instructionset.get(i));
                for (InstructionSet y : instructionset) {
                    if (x.getInstruction().equals(y.getOperationname()) || x.getInstruction().startsWith("=") || x.getInstruction().startsWith("$")
                            || x.getInstruction().startsWith("+") || x.getLabel().equals(".")) {
                        flag = 1;
                        break;
                    } else

                        flag = 0;

                }
                if (flag == 0) {
                    System.out.println("ERROR");
                    System.exit(1);
                }

            }
        }
    }


    public void locr() throws IOException {


        File f = new File("LocationCounter.txt");
        FileWriter w = new FileWriter(f);

        for (AssemblyCode x : code) {

            if (x.getLabel().equals(".")) {
                w.write(" ");
                w.write(System.lineSeparator());

            } else if (x.getInstruction().equals("END")) {
                w.write("-----" + "\t" + x.getLabel() + "\t" + x.getInstruction() + "\t");
                w.write(System.lineSeparator());

            } else {
                w.write(x.getAdress() + "\t" + x.getLabel() + "\t" + x.getInstruction() + "\t");
                w.write(System.lineSeparator());

            }

        }
        w.close();
    }


    public void literal() throws IOException {
        File f = new File("Literal.txt");
        FileWriter w = new FileWriter(f);

        for (AssemblyCode x : code) {


            if (!x.getInstruction().startsWith("=X")) {

                if (x.getValue().startsWith("=") || x.getInstruction().equals("LTRORG")) {


                    if (x.getValue().startsWith("=")) {
                        //System.out.println(x.getValue());
                        y = x.getValue();
                        z = x.getAdress();



                    } else if (x.getInstruction().equals("LTRORG")) {

                        a = x.getAdress();

                        int ahex = Integer.parseInt(a, 16);
                        int bhex = Integer.parseInt(z, 16);
                        //System.out.println("ahex");
                        //System.out.println(ahex);
                        bhex = bhex + 0x3;
                        ahex = ahex - bhex;
                        String addressInHex = Integer.toHexString(ahex);
                        //System.out.println("addresshexa");
                        //System.out.println(addressInHex);
                        w.write(y);
                        w.write(" = ");
                        w.write("00" + addressInHex);
                        w.write(System.lineSeparator());
                        //System.out.println(a);
                        //System.out.println(z);
                        //System.out.println(bhex);
                        //System.out.println(ahex-bhex);

                    }


                } else if (x.getInstruction().equals("END")) {
                    //System.out.println("hi");
                    end = x.getAdress();

                    //System.out.println(end);
                    //System.out.println(end);
                }
            } else {
                w.write(x.getInstruction());
                w.write(" = ");
                w.write(end);
                w.write(System.lineSeparator());
            }


        }


        w.close();
        System.out.println(" ");
    }

    public void literal2() throws IOException {

        String a = "";
        int temp;
        String temps="";
        String opcode="";
        int optemp;
        String opbin="";
        String allhex="";
        String ad="";
        int adhexx = 0;
        String bla="";
        int f =0;
        int j=0;
        int k=0;
        int addressX=0;
        for (int i = 1; i < code.size(); i++) {
        String v= code.get(i).getValue();
        String add = code.get(i).getAdress();
        String inst = code.get(i).getInstruction();


            if (inst.startsWith("=X")) {

                String s = inst;
                String b = s.substring(3);
                //System.out.println(b);
                code.get(i).setOpcode(b);
                String adx = code.get(i).getAdress();

                addressX=Integer.parseInt(adx, 16);




                ad= Integer.toHexString(adhexx);
                code.get(k).setOpcode(ad);

            }


            if (v.startsWith("=X")) {

                opcode = OpcodeGenerator(inst);
                optemp = Integer.parseInt(opcode, 16);
                opbin = Integer.toBinaryString(optemp);
                opbin = opbin.substring(0, opbin.length() - 2);
                String reg = "110010";
                opbin = opbin + reg;
                int bintemp = Integer.parseInt(opbin, 2);
                allhex = Integer.toHexString(bintemp);


                ad = code.get(i+1).getAdress();
                adhexx = Integer.parseInt(ad, 16);



                k=i;



            }


            if (v.startsWith("=C")) {



                 opcode = OpcodeGenerator(inst);
               // System.out.println(opcode);
                 optemp = Integer.parseInt(opcode, 16);
                 opbin = Integer.toBinaryString(optemp);
                opbin = opbin.substring(0, opbin.length() - 2);
                //System.out.println("blbl");
                //System.out.println(opbin);

                String reg = "110010";
                opbin = opbin + reg;
                int bintemp = Integer.parseInt(opbin, 2);
                 allhex = Integer.toHexString(bintemp);




                 ad = code.get(i+1).getAdress();

                 adhexx = Integer.parseInt(ad, 16);
                //String adhex= Integer.toHexString(adhexx);
                //System.out.println("adhex");
                //System.out.println(adhex);

                j =i;
                //int add1 = Integer.parseInt(ad, 16);
                //System.out.println("adduni");
               // System.out.println(adduni);
                //System.out.println("addhex");
                //System.out.println(adhexx);
                //int add1= adduni - adhexx;
                //System.out.println("add1");
               // System.out.println(add1);


               // ad = Integer.toBinaryString(add1);

                //System.out.println("bla");
                //System.out.println(ad);



                //System.out.println("ahex");
                //System.out.println(bla);


            }

            if (inst.equals("LTRORG")) {
                String a2 = add;
                a = code.get(i+2).getAdress();
                int ahex = Integer.parseInt(a, 16);
                ahex = ahex - adhexx;
                 bla= "00" + Integer.toHexString(ahex);
                //System.out.println("ahex");
                //System.out.println(bla);


                //a= Integer.toHexString(adduni);

            code.get(j).setOpcode(bla);

            }

        }


        }






    public void PassTwo() {

        for (AssemblyCode x : code) {
            if(x.getInstruction().equals("BASE"))
            {
                base = symtab.get(x.getValue());
                org = x.getValue();
                System.out.println("Base");
                System.out.println(base);

            }

        }
        for (int i = 1; i < code.size(); i++) {
            if (!code.get(i).getInstruction().equals("END") &&
                    !code.get(i).getInstruction().equals("BASE") && !code.get(i).getInstruction().equals("LTRORG") &&
                    !code.get(i).getLabel().equals("*") && !code.get(i).getInstruction().equals("START") &&
                    !code.get(i).getInstruction().equals("EQU")) {
                String instruction = code.get(i).getInstruction();
                String value = code.get(i).getValue();
                String opcode = OpcodeGenerator(instruction);
                String address = symtab.get(value);


                if (instruction.equals("RESB") || instruction.equals("RESW") || instruction.equals("RESDW") ||
                        instruction.equals("EQ") || instruction.equals("LTRORG") ) {
                    code.get(i).setOpcode("");

                }

                else if(instruction.equals("RSUB"))
                    code.get(i).setOpcode("4f0000");
                else if (instruction.equals("WORD")) {
                    String objectcode = Integer.toHexString(Integer.parseInt(value));
                    while (objectcode.length() < 6) {
                        objectcode = Refill(objectcode,6);
                    }
                    code.get(i).setOpcode(objectcode);

                }


                else if (instruction.equals("BYTE")) {
                    if (value.startsWith("X")) {
                        // Remove X from opcode printing
                        value = value.replace("X", "");
                        // Remove '' from opcode printing
                        value = value.replace("'", "");

                        code.get(i).setOpcode(value);

                    } else if (value.startsWith("C")) {
                        String opcode2 = "";
                        //J STARTS WITH 2 SINCE WE IGNORE ' & C
                        for (int j = 2; j < value.length() - 1; j++) {
                            {
                                opcode2 = opcode2 + Integer.toHexString(value.charAt(j));
                            }
                            code.get(i).setOpcode(opcode2);

                        }
                    }
                }
                else if(instruction.startsWith("$"))

                {



                    String inst = instruction.substring(1);
                    String instop = OpcodeGenerator(inst);
                    String opcodetemp = instop;
                    String allhex = null;
                    String tempv2 = value;
                    //Opcode to binary
                    //System.out.println(instop);
                    int optemp = Integer.parseInt(opcodetemp, 16);
                    String opbin = Integer.toBinaryString(optemp);
                    opbin = opbin.substring(0, opbin.length() - 2);



                    //displacement

                    //System.out.println("hi");
                    tempv2 = symtab.get(value);
                    System.out.println(tempv2);
                    int optemp2= Integer.parseInt(tempv2,16);

                    String location = code.get(i).getAdress();
                    //System.out.println(location);
                    int lochex = Integer.parseInt(location, 16);
                    //System.out.println(lochex);
                    //System.out.println(optemp2);
                    lochex =  lochex + 0x5;
                    //System.out.println(lochex);



                    optemp2 = optemp2-lochex;
                    if(optemp2%2==0)
                    {
                        //6 registers + opcode to hex
                        String reg = "100010";
                        String binarypart = opbin + reg;
                        int bintemp = Integer.parseInt(binarypart, 2);
                        allhex = Integer.toHexString(bintemp);
                        //System.out.println(binarypart);
                    }
                    if(optemp2%2==1)
                    {
                        //6 registers + opcode to hex
                        String reg = "010010";
                        String binarypart = opbin + reg;
                        int bintemp = Integer.parseInt(binarypart, 2);
                        allhex = Integer.toHexString(bintemp);
                        //System.out.println(binarypart);
                    }


                    //System.out.println(optemp2);
                    String addressInHex = Integer.toHexString(optemp2);
                    //System.out.println(addressInHex);
                    //addressInHex = "0" + allhex + "0" + addressInHex;
                    if(addressInHex.length()==1)
                        addressInHex = allhex +"0" + "0" + addressInHex;
                    else if (addressInHex.length()==2)
                        addressInHex =allhex +"0"  + addressInHex;
                    else if(addressInHex.length()==3)
                        addressInHex = allhex + addressInHex;

                    if(addressInHex.length()==4)
                        addressInHex =  "0" + "0" + addressInHex;
                    else if (addressInHex.length()==5)
                        addressInHex =  "0" + addressInHex;
                    else if(addressInHex.length()==6)
                        addressInHex =  addressInHex;








                    code.get(i).setOpcode(addressInHex);
                    //System.out.println("bye");







                }


                else if (instruction.equals("FIX") || instruction.equals("SIO") || instruction.equals("FLOAT")
                        || instruction.equals("HIO") || instruction.equals("TIO"))

                {
                    code.get(i).setOpcode(OpcodeGenerator(instruction));

                }

                else if (instruction.equals("DIVR") || instruction.equals("CLEAR") || instruction.equals("COMPR")
                        || instruction.equals("ADDR") || instruction.equals("MULR")||instruction.equals("RMO")
                        || instruction.equals("SHIFTR") || instruction.equals("SUBR") || instruction.equals("SVC")||
                        instruction.equals ("TIXR") || instruction.equals("SHIFTL"))
                {

                    String op =  OpcodeGenerator(instruction);
                    String reg=null;
                    String reg2=null;
                    String address1=null;
                    if(value.length()==1)
                    {
                        if (value.equals("A"))
                            reg = "0";
                        if (value.equals("X"))
                            reg = "1";
                        if (value.equals("L"))
                            reg = "2";
                        if (value.equals("PC"))
                            reg = "8";
                        if (value.equals("SW"))
                            reg = "9";
                        if (value.equals("B"))
                            reg = "3";
                        if (value.equals("S"))
                            reg = "4";
                        if (value.equals("T"))
                            reg = "5";
                        if (value.equals("F"))
                            reg = "6";
                        reg2="0";
                        address1 = op+reg+reg2;
                        code.get(i).setOpcode(address1);
                    }
                    if(value.length()==3)
                    {



                        if (value.startsWith("A"))
                            reg = "0";
                        if (value.startsWith("X"))
                            reg = "1";
                        if (value.startsWith("L"))
                            reg = "2";
                        if (value.startsWith("PC"))
                            reg = "8";
                        if (value.startsWith("SW"))
                            reg = "9";
                        if (value.startsWith("B"))
                            reg = "3";
                        if (value.startsWith("S"))
                            reg = "4";
                        if (value.startsWith("T"))
                            reg = "5";
                        if (value.startsWith("F"))
                            reg = "6";


                        if (value.endsWith("A"))
                            reg2 = "0";
                        if (value.endsWith("X"))
                            reg2 = "1";
                        if (value.endsWith("L"))
                            reg2 = "2";
                        if (value.endsWith("PC"))
                            reg2 = "8";
                        if (value.endsWith("SW"))
                            reg2 = "9";
                        if (value.endsWith("B"))
                            reg2 = "3";
                        if (value.endsWith("S"))
                            reg2 = "4";
                        if (value.endsWith("T"))
                            reg2 = "5";
                        if (value.endsWith("F"))
                            reg2 = "6";

                        address1 = op+reg+reg2;
                        code.get(i).setOpcode(address1);
                    }

                }






                else if (instruction.startsWith("+"))
                {
                    String inst = instruction.substring(1);
                    String instop = OpcodeGenerator(inst);
                    String opcodetemp = instop;
                    String addressInHex;
                    String reg;
                    //System.out.println(instop);
                    // System.out.println(instop);
                    int optemp = Integer.parseInt(opcodetemp, 16);
                    String opbin = Integer.toBinaryString(optemp);
                    opbin = opbin.substring(0, opbin.length() - 2);
                    //System.out.println(opbin);

                    //6 registers + opcode to hex
                    if(value.startsWith("#"))
                        reg = "010001";
                    else
                        reg = "110001";

                    String binarypart = opbin + reg;
                    int bintemp = Integer.parseInt(binarypart, 2);
                    String allhex = Integer.toHexString(bintemp);
                    //System.out.println(allhex);
                    if(value.startsWith("#"))
                    { String val = value.substring(1);
                        int vv=Integer.parseInt(val);
                        String s= decToHexa(vv);

                        if (s.length() == 1)
                            s = "0000" + s;
                        else if (s.length() == 2)
                            s = "000" + s;
                        else if (s.length() == 3)
                            s = "00" + s;
                        else if (s.length() == 4)
                            s = "0" + s;
                        else if (s.length() == 5)
                            s = s;

                        addressInHex = allhex + s;
                        if (addressInHex.length() == 7)
                            addressInHex = "0" + addressInHex;
                        else if (addressInHex.length() == 6)
                            addressInHex = "00" + addressInHex;
                        else if (addressInHex.length() == 5)
                            addressInHex = "000" + addressInHex;
                        else if (addressInHex.length() == 4)
                            addressInHex = "0000" + addressInHex;

                    }

                    else {
                        //displacement
                        String tempv2 = value;
                        //System.out.println("hi");
                        tempv2 = symtab.get(tempv2);
                        //System.out.println(tempv2);

                        if (tempv2.length() == 1)
                            tempv2 = "0000" + tempv2;
                        else if (tempv2.length() == 2)
                            tempv2 = "000" + tempv2;
                        else if (tempv2.length() == 3)
                            tempv2 = "00" + tempv2;
                        else if (tempv2.length() == 4)
                            tempv2 = "0" + tempv2;
                        else if (tempv2.length() == 5)
                            tempv2 = tempv2;

                        addressInHex = allhex + tempv2;
                        if (addressInHex.length() == 7)
                            addressInHex = "0" + addressInHex;
                        else if (addressInHex.length() == 6)
                            addressInHex = "00" + addressInHex;
                        else if (addressInHex.length() == 5)
                            addressInHex = "000" + addressInHex;
                        else if (addressInHex.length() == 4)
                            addressInHex = "0000" + addressInHex;
                    }


                    code.get(i).setOpcode(addressInHex);


                }





                else if (!instruction.startsWith("+") && !instruction.startsWith("$"))

                {
                    String opbin=null;
                    if (value.startsWith("#")) {

                        String tempv = value.substring(1);
                        if (isNumeric(tempv)) {
                            //Opcode to binary
                            String opcodetemp = opcode;
                            //System.out.println(opcodetemp);
                            if(instruction.equals("LDA"))
                                opbin = "000000";
                            else {
                                int optemp = Integer.parseInt(opcodetemp, 16);
                                opbin = Integer.toBinaryString(optemp);
                                opbin = opbin.substring(0, opbin.length() - 2);
                                //System.out.println(opbin);
                            }
                            //6 registers + opcode to hex
                            String reg = "010000";

                            String binarypart = opbin + reg;
                            int bintemp = Integer.parseInt(binarypart, 2);
                            String allhex = Integer.toHexString(bintemp);
                            //System.out.println(binarypart);
                            //System.out.println(allhex);

                            //displacement




                            if(tempv.length()==1)
                                tempv = allhex + "0" + "0" + tempv;
                            else if (tempv.length()==2)
                                tempv = allhex + "0" + tempv;
                            else if(tempv.length()==3)
                                tempv = allhex  + tempv;


                            if(allhex.length()==4)
                                allhex ="0" + "0" + allhex;
                            else if (tempv.length()==5)
                                allhex ="0"  + allhex;
                            else if(tempv.length()==6)
                                allhex = allhex;



                            //System.out.println(tempv);
                            code.get(i).setOpcode(tempv);


                        }


                        else
                        {

                            String tempv2 = value.substring(1);
                            //Opcode to binary
                            String opcodetemp = opcode;
                            //System.out.println(opcodetemp);
                            if(instruction.equals("LDA"))
                                opbin = "000000";
                            else {
                                int optemp = Integer.parseInt(opcodetemp, 16);
                                opbin = Integer.toBinaryString(optemp);
                                opbin = opbin.substring(0, opbin.length() - 2);
                            }
                            //6 registers + opcode to hex
                            String reg = "010010";
                            String binarypart = opbin + reg;
                            int bintemp = Integer.parseInt(binarypart, 2);
                            String allhex = Integer.toHexString(bintemp);
                            //System.out.println(binarypart);

                            //displacement

                            //System.out.println("hi");
                            tempv2 = symtab.get(tempv2);
                            //System.out.println(tempv2);
                            int optemp2= Integer.parseInt(tempv2,16);

                            String location = code.get(i+1).getAdress();
                            //System.out.println(location);
                            int lochex = Integer.parseInt(location, 16);
                            //System.out.println(lochex);
                            //System.out.println(optemp2);
                            //lochex =  lochex + 0x3;
                            //System.out.println(lochex);



                            optemp2 = optemp2-lochex;
                            //System.out.println(optemp2);
                            String addressInHex = Integer.toHexString(optemp2);
                            //System.out.println(addressInHex);
                            //addressInHex = "0" + allhex + "0" + addressInHex;

                            if(addressInHex.length()==1)
                                addressInHex = allhex +"0" + "0" + addressInHex;
                            else if (addressInHex.length()==2)
                                addressInHex =allhex +"0"  + addressInHex;
                            else if(addressInHex.length()==3)
                                addressInHex = allhex + addressInHex;

                            if(addressInHex.length()==4)
                                addressInHex =  "0" + "0" + addressInHex;
                            else if (addressInHex.length()==5)
                                addressInHex =  "0" + addressInHex;
                            else if(addressInHex.length()==6)
                                addressInHex =  addressInHex;







                            code.get(i).setOpcode(addressInHex);
                            //System.out.println("bye");



                        }




                    }

                    else if (value.endsWith(",X"))
                    // as in buffer,x
                    // remove X from printing
                    {
                        String v = value.substring(0, value.length() - 2);
                        //System.out.println(v);
                        String allhex = null;
                        String reg=null;
                        String tempv2 = symtab.get(v);
                        //System.out.println(v);
                        //Opcode to binary
                        String opcodetemp = opcode;
//System.out.println(opcodetemp);
                        int optemp = Integer.parseInt(opcodetemp, 16);
                        opbin = Integer.toBinaryString(optemp);
                        opbin = opbin.substring(0, opbin.length() - 2);

                        String location = code.get(i+1).getAdress();
                        //System.out.println(location);
                        int lochex = Integer.parseInt(location, 16);
                        //System.out.println(lochex);
                        //System.out.println(optemp2);
                        lochex =  lochex ;
                        //System.out.println(lochex);
                        int optemp2= Integer.parseInt(tempv2,16);
                        int tempo = optemp2;
                        optemp2 = optemp2-lochex;
                        //System.out.println(optemp2);
                        String addressInHex = Integer.toHexString(optemp2);


                        //System.out.println(addressInHex);
                        //addressInHex = "0" + allhex + "0" + addressInHex;




                        if(addressInHex.length()>3) {
                            //addressInHex = twosCompliment(addressInHex);

                            reg = "111100";

                            //int binreg= Integer.parseInt(reg,16);
                            String binreg =opbin + reg;
                            //System.out.println(binreg);
                            int bintemp = Integer.parseInt(binreg, 2);
                            allhex = Integer.toHexString(bintemp);
                            //binreg= binreg.toHexString(binreg1);
                            //System.out.println("tempo");
                            //System.out.println(base);
                            tempo = Integer.parseInt(symtab.get(v)) - Integer.parseInt(base);
                            //System.out.println("minus");
                            //System.out.println(tempo);
                            addressInHex = Integer.toHexString(tempo);
                            //addressInHex =  allhex + addressInHex;
                            //System.out.println("nananan");
                            //System.out.println(addressInHex);
                            //System.out.println("nananan");

                        }
                        if (addressInHex.length()==8) {
                            //System.out.println("hey");
                            //System.out.println(addressInHex);
                            addressInHex =addressInHex.substring(5);
                            //System.out.println(addressInHex);
                        }





                        //6 registers + opcode to hex


                        //dis

                        //System.out.println(tempv2);




                        //reg = "111010";
                        //String binarypart = opbin + reg;
                        //int bintemp = Integer.parseInt(binarypart, 2);
                        //String allhex = Integer.toHexString(bintemp);





                        if(addressInHex.length()==1)
                            addressInHex = allhex +"0" + "0" + addressInHex;
                        else if (addressInHex.length()==2)
                            addressInHex =allhex +"0"  + addressInHex;
                        else if(addressInHex.length()==3)
                            addressInHex = allhex + addressInHex;

                        if(addressInHex.length()==4)
                            addressInHex =  "0" + "0" + addressInHex;
                        else if (addressInHex.length()==5)
                            addressInHex =  "0" + addressInHex;
                        else if(addressInHex.length()==6)
                            addressInHex =  addressInHex;

                        code.get(i).setOpcode(addressInHex);

                    }


                    else if (value.startsWith("@")) {
                        String tempv2 = value.substring(1);
                        //Opcode to binary
                        String opcodetemp = opcode;
                        //System.out.println(opcodetemp);
                        if(instruction.equals("LDA"))
                            opbin = "000000";
                        else {
                            int optemp = Integer.parseInt(opcodetemp, 16);
                            opbin = Integer.toBinaryString(optemp);
                            opbin = opbin.substring(0, opbin.length() - 2);
                        }
                        //6 registers + opcode to hex
                        String reg = "100010";
                        String binarypart = opbin + reg;
                        int bintemp = Integer.parseInt(binarypart, 2);
                        String allhex = Integer.toHexString(bintemp);
                        //System.out.println(binarypart);

                        //displacement

                        //System.out.println("hi");
                        tempv2 = symtab.get(tempv2);
                        //System.out.println(tempv2);
                        int optemp2= Integer.parseInt(tempv2,16);

                        String location = code.get(i+1).getAdress();
                        //System.out.println(location);
                        int lochex = Integer.parseInt(location, 16);
                        //System.out.println(lochex);
                        //System.out.println(optemp2);
                        lochex =  lochex ;
                        //System.out.println(lochex);



                        optemp2 = optemp2-lochex;
                        //System.out.println(optemp2);
                        String addressInHex = Integer.toHexString(optemp2);
                        //System.out.println(addressInHex);
                        //addressInHex = "0" + allhex + "0" + addressInHex;
                        if(addressInHex.length()==1)
                            addressInHex = allhex +"0" + "0" + addressInHex;
                        else if (addressInHex.length()==2)
                            addressInHex =allhex +"0"  + addressInHex;
                        else if(addressInHex.length()==3)
                            addressInHex = allhex + addressInHex;

                        if(addressInHex.length()==4)
                            addressInHex =  "0" + "0" + addressInHex;
                        else if (addressInHex.length()==5)
                            addressInHex =  "0" + addressInHex;
                        else if(addressInHex.length()==6)
                            addressInHex =  addressInHex;

                        code.get(i).setOpcode(addressInHex);
                        //System.out.println("bye");



                    }




                    else if (!value.startsWith("@") && !value.startsWith("#") && !value.endsWith(",X") &&
                            !(instruction.equals("LTRORG")) && !(value.startsWith("=")) && !(instruction.startsWith("=")))
                    {

                        String tempv2 = value;
                        //Opcode to binary
                        String opcodetemp = OpcodeGenerator(instruction);
                        //System.out.println(opcodetemp);
                        if(instruction.equals("LDA"))
                            opbin = "000000";
                        else {

                            int optemp = Integer.parseInt(opcodetemp, 16);
                            opbin = Integer.toBinaryString(optemp);
                            //System.out.println("hi");
                            //System.out.println(opbin);

                            opbin = opbin.substring(0, opbin.length() - 2);
                        }
                        //6 registers + opcode to hex
                        String reg = "110010";
                        String binarypart = opbin + reg;
                        int bintemp = Integer.parseInt(binarypart, 2);
                        String allhex = Integer.toHexString(bintemp);
                        //System.out.println(binarypart);

                        //displacement

                        //System.out.println("hi");
                        //System.out.println(tempv2);
                        tempv2 = symtab.get(tempv2);
                        //System.out.println(tempv2);
                        // System.out.println(code.get(i).getAdress());
                        //System.out.println(tempv2);
                        //System.out.println(tempv2);
                        int optemp2= Integer.parseInt(tempv2,16);

                        String location = code.get(i+1).getAdress();
                        //System.out.println(location);
                        int lochex = Integer.parseInt(location, 16);
                        //System.out.println(lochex);
                        //System.out.println(optemp2);
                        lochex =  lochex;
                        //System.out.println(lochex);


                        optemp2 = optemp2-lochex;
                        //System.out.println(optemp2);
                        String addressInHex = Integer.toHexString(optemp2);
                        if(addressInHex.startsWith("-")) {
                            addressInHex = twosCompliment(addressInHex);
                            //System.out.println("hey");
                            addressInHex.substring(1, addressInHex.length()-3);

                        }

                        //System.out.println(addressInHex);
                        //addressInHex = "0" + allhex + "0" + addressInHex;







                        if (addressInHex.length()==8) {
                            //System.out.println("hey");
                            //System.out.println(addressInHex);
                            addressInHex =addressInHex.substring(5);
                            // System.out.println(addressInHex);
                        }

                        if(addressInHex.length()==1)
                            addressInHex = allhex +"0" + "0" + addressInHex;
                        else if (addressInHex.length()==2)
                            addressInHex =allhex +"0"  + addressInHex;
                        else if(addressInHex.length()==3)
                            addressInHex = allhex + addressInHex;

                        if(addressInHex.length()==4)
                            addressInHex =  "0" + "0" + addressInHex;
                        else if (addressInHex.length()==5)
                            addressInHex =  "0" + addressInHex;
                        else if(addressInHex.length()==6)
                            addressInHex =  addressInHex;



                        code.get(i).setOpcode(addressInHex);
                        //System.out.println("bye");



                    }








                }
            }


        }




// printing whole program
        for (AssemblyCode x : code) {
            int i = 0;
            if (code.get(i).getInstruction().equals("END") &&
                    code.get(i).getInstruction().equals("BASE") && code.get(i).getInstruction().equals("LTRORG") &&
                    code.get(i).getLabel().equals("*") && code.get(i).getInstruction().equals("START") &&
                    code.get(i).getInstruction().equals("EQU"))
                System.out.println(x.getAdress() + "\t" + x.getLabel() + "\t" + x.getInstruction() + "\t" + x.getValue());
            else
                System.out.println(x.getAdress() + "\t" + x.getLabel() + "\t" + x.getInstruction() + "\t" + x.getValue() + "\t" + x.getOpcode());
        }

    }







    public String OpcodeGenerator(String Instruction) {
        for (InstructionSet x : instructionset) {
            if (x.getOperationname().equals(Instruction)) {
                return x.getOpcode();
            }

        }
        return "End of code -- ";
    }

    public static boolean isNumeric(final String str) {

        // null or empty
        if (str == null || str.length() == 0) {
            return false;
        }

        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;

    }

    public String twosCompliment(String bin) {
        String twos = "", ones = "";
        int j = 0;

        for (int i = 0; i < bin.length(); i++) {
            ones += flip(bin.charAt(i));
        }
        int number0 = Integer.parseInt(ones, 2);
        StringBuilder builder = new StringBuilder(ones);
        boolean b = false;
        for (j = 0; j < 3; j++) {
            for (int i = ones.length() - 1; i > 0; i--) {
                if (ones.charAt(i) == '1') {
                    builder.setCharAt(i, '0');
                } else {
                    builder.setCharAt(i, '1');
                    b = true;
                    break;
                }
            }
        }
        if (!b)
            builder.append("1", 0, 2);

        twos = builder.toString();



        return twos;
    }
    public char flip(char c) {
        return (c == '0') ? '1' : '0';
    }

    public static String decToHexa(int decimal){
        int rem;
        String hex="";
        char hexchars[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        while(decimal>0)
        {
            rem=decimal%16;
            hex=hexchars[rem]+hex;
            decimal=decimal/16;
        }
        return hex;
    }

    public void MRECORD() throws IOException {
        for (AssemblyCode x : code)
        {
            if(x.getInstruction().startsWith("+") &&! x.getValue().startsWith("#"))
            {
                System.out.print("M.");

                int lochex = Integer.parseInt(x.getAdress(), 16);
                lochex = lochex+0x1;
                String addressInHex = Integer.toHexString(lochex);
                if(addressInHex.length()==3)
                    addressInHex= "0" +addressInHex;
                if(addressInHex.length()==2)
                    addressInHex= "00" +addressInHex;
                if(addressInHex.length()==1)
                    addressInHex= "000" +addressInHex;
                System.out.print(addressInHex);

                System.out.print(".05");
                System.out.println("");
            }



        }


    }

    public void HTE_Record() throws IOException {

        int i = 0;
        int j = 0;
        File f = new File("HTE.txt");
        FileWriter w = new FileWriter(f);
        String lab = code.get(0).getLabel();
        while (lab.length() < 6) {
            lab = lab + " ";
        }
        String start = code.get(0).getValue();
        String end = code.get(code.size() - 1).getAdress();
        int start2 = Integer.parseInt(start, 16);
        int end2 = Integer.parseInt(end, 16);
        int length = end2 - start2;
        String programLength = Integer.toHexString(length);
        while (programLength.length() < 6) {
            programLength = Refill(programLength, 6);
        }
        while (start.length() < 6) {
            start = Refill(start, 6);
        }

        System.out.println("H" + "." + lab + "." + start + "." + programLength);
        w.write("H" + "." + lab + "." + start + "." + programLength);
        w.write(System.lineSeparator());
        System.out.println("E" + "." + start);
        w.write("E" + "." + start);
        w.close();
        int sum = 0;
        int l = 0;
        System.out.print("T");
        for (int k = 0; k < 5; k++) {


            while (sum <= 55 || code.get(l--).getInstruction().equals("RESW") || code.get(l--).getInstruction().equals("RESB")) {




                System.out.print(code.get(l).getOpcode());
                sum = sum + code.get(l).getOpcode().length();
                System.out.print(".");
                l++;

            }


            sum = 0;
            System.out.println("");
            System.out.print("T");


        }


    }
}

