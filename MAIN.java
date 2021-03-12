
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class MAIN {
    public static void main(String[] args) throws  IOException {
        // TODO code application logic here
        SicAssembler myassembler =new SicAssembler();
        myassembler.ReadAssemblyCode();
        myassembler.ScanInstructionSet();
        myassembler.PassOne();
        myassembler.locr();
        myassembler.literal();
        myassembler.literal2();
        myassembler.PassTwo();
        myassembler.MRECORD();
        myassembler.HTE_Record();

    }


}
