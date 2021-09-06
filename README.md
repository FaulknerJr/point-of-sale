Keenan’s Point Of Sale
Keenan’s is a convenience store chain in Phoenix, Arizona. The company wants you to build
software for a point-of-sale terminal with the following requirements:

● On start, the terminal loads product information from a CSV file specified in Appendix 1.
You should assume the product information is well-formatted

● The cashier interacts with the software in a console via stdin and stdout

● Items are rung up by product identifier

● Since the barcode reader is broken, products are entered by manually typing the 12
decimal digit product identifier followed by a newline character to stdin upon successfully
reading a barcode

● If a partial product identifier number is entered the terminal should output a listing of
product IDs and descriptions where the initial digits of the product ID match what was
entered. However, if the prefix entered matches only a single product ID, then that
product should be rung up. The algorithm should have asymptotic performance superior
to O(n)

● When a product is rung up the name and price should be output

● When all items are rung up, the cashier instructs the terminal to total the bill and the
terminal should output the subtotal, total tax and total amount due

● After totaling, the cashier enters the amount customer paid

● Tax rates are 6.3% for state, 0.7% for county and 2.0% for city. State and county do not
tax grocery items. City taxes all items.

● After receiving the amount paid, the terminal outputs a receipt to stdout, including the
following. (Do not worry too much about formating receipt)

○ on a separate line for each item rung up, the following values: product name,
product identifier, price, and tax category

○ subtotal

○ tax for each jurisdiction

○ total due

○ amount paid

○ change due

Appendix 1

The CSV file conforms to RFC 4180 and does not contain a header. The columns in order are
1. product identifier, unique string of 12 decimal digits
2. product name, short string for display
3. current price, decimal number
4. tax category, one of the following
a. g (groceries)
b. pf (prepared food)
c. pd (prescription drug)
d. nd (non-prescription drug)
e. c (clothing)
f. o (other items)


Sample data:
017082112774,JK LNK BEEF TERI,5.99,g 
018200530470,BUD LT 12 CAN,11.99,o 
028400157827,CHEETOS CHED JAL,3.99,g 
028400589864,CHEETOS CRUNCHY,3.99,g 
080660956756,CORONA LT 12 BTL,12.99,o 
305730133203 ,ADVIL IBU 20CT,7.99,n 
051000199447,CAMPBELL GO COCO,4.69,g 
051000058874,CAMPBELL HLTH TOM,2.49,g 
051000195654,CAMPBELL HOME SW,3.99,g 
305732450421,NEXIUM 24H ACID,8.99,nd 
305730184328,ADVIL COLD SINUS,6.99,nd 
305730179201,ADVIL JR IBUPROF,4.99,nd 