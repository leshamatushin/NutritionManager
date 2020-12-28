"use strict";

let selectedRow = null;
let addProductButton = document.getElementById("addProductButton");
let updateProductButton = document.getElementById("updateProductButton");

addProductButton.href="/nutritionManager-1.0.0/product?dataType=newProduct&newTab=true";
document.getElementById("removeProductButton").onclick = removeProduct;

async function removeProduct() {
	if(selectedRow !== null) {
		let response = await fetch("/nutritionManager-1.0.0/products" , {
			method: "DELETE",
			headers: {
				"Content-Type": "text/plain"
			},
			body: selectedRow.id
		});
		
		if(response.ok) {
			await loadProducts(0, 50);
		} else {
			throw new Error("Не удалось загрузить удалить продуктов.");
		}
	}
}

function createProductsTableCell(data, row, cellIndex) {
	let cell = row.insertCell(cellIndex);
	cell.style="text-align:center;"
	cell.appendChild(document.createTextNode(data));
	return cell;
}

async function loadProducts(fromIndex, count) {
	let response = await fetch(`/nutritionManager-1.0.0/products?dataType=productsList&from=${fromIndex}&count=${count}`);
	
	if(response.ok) {
		let products = await response.json();
		let table = document.getElementById("tableId");
		
		for(let i = table.rows.length - 1; i > 0; --i) table.deleteRow(i);
		
		for(let product of products) {
			let row = table.insertRow(-1);
			
			row.id = product.ID;
			
			row.onclick = function() {
				if(selectedRow !== null) selectedRow.classList.remove("selectRow");
				row.classList.add("selectRow");
				selectedRow = row;
				updateProductButton.href=`/nutritionManager-1.0.0/product?dataType=existsProduct&productId=${product.ID}&newTab=true`;
			};
			
			createProductsTableCell(product.NAME, row, 0);
			createProductsTableCell(product.UNIT, row, 1);
			createProductsTableCell(product.AVERAGE_PRICE, row, 2);
			createProductsTableCell(product.AVERAGE_CALORIES, row, 3);
		}
	} else {
		throw new Error("Не удалось загрузить список продуктов. " + response.status);
	}
}
	
	
	
loadProducts(0, 50);	
	