var product;
function setProduct(p){
	product = p;
}
var isInCart;
function isInCart(c){
	isInCart = c;
}
document.addEventListener('DOMContentLoaded', function() {
	
	var price = document.getElementById("price");
	var q = document.getElementById("qty");
	var b = document.getElementById("qtyBeer");
	var s = document.getElementById("size");
	
		if(!(isInCart)){
			updateQty(6);//sets untouched product price
		}
		
		if(b!==null){
			b.onchange = function(){
				updateQty(b.options[b.selectedIndex].value);	
			}
		}
		
		if(q!==null){
			q.onchange = function() {
				var qty = q.options[q.selectedIndex].value;
				if(product.catId=="21"){
					updateWeight(parseInt(s.options[s.selectedIndex].value),qty);
				}else{
					updateQty(qty);
				}
			};	
		}
	
	if(product.catId=="21"){
		if(s!==null){
			s.onchange = function() {
				var qty = q.options[q.selectedIndex].value;
				updateWeight(parseInt(s.options[s.selectedIndex].value),qty);
			};	
		}
	}
	function updateWeight(weight, qty) {
		var amount;
		switch (weight){
			case 375:
				amount = product.price;
			break;
			
			case 750:
				amount = product.price + 25;
			break;
			
			case 1140:
				amount = product.price + 40;
			break;
		}
		if(product.weight.length <= 6){
			amount = product.price;
		}
		amount = amount * qty ;
		price.innerHTML= "$"+ amount +".00";
	}
	function updateQty(quantity) {
	var amount;
	if(quantity<=3) {
		amount = product.price * quantity;
	}
	else if(quantity<=24){
		amount = parseInt(product.price,10) + (15.00*((quantity/6)-1));
	}
	else if(quantity==36){
		amount = 125;
	}
	else if(quantity==48){
		amount = 155;
	}
	else if(quantity==60){
		amount = 195;
	}
	else if(quantity==72){
		amount = 230;
	}
	if(product.title.includes("Tall Boys") || product.weight.includes("473")){//pilsner // white claw //jaw drops
		if(quantity >= 48) amount += 10;
		if(quantity==72) amount = 245;
	}
	price.innerHTML= "$"+ amount +".00";
};
});

