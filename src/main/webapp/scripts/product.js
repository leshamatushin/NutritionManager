let url = new URL(window.location.href);

let saveProductButton = document.getElementById("saveProductButton");
let backToProductsButton = document.getElementById("cancelProductButton");

let productNameHeader = document.getElementById("productNameHeader");
let productNameInput = document.getElementById("productNameInput");
let productUnitInput = document.getElementById("productUnitInput");
let productAveragePriceInput = document.getElementById("productAveragePriceInput");
let productAverageCaloriesInput = document.getElementById("productAverageCaloriesInput");

let productPricesList = document.getElementById("productPricesList");
let productCaloriesList = document.getElementById("productCaloriesList");

let addPriceButton = document.getElementById("addPriceButton");
let removePriceButton = document.getElementById("removePriceButton");
let addCaloriesButton = document.getElementById("addCaloriesButton");
let removeCaloriesButton = document.getElementById("removeCaloriesButton");

let selectedProductPriceContainer = null;
let selectedProductCaloriesContainer = null;



saveProductButton.onclick = async function() {
	let response = await fetch(url, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json;charset=utf-8'
		},
		body: JSON.stringify(formDataToProduct())
	});
	
	if(response.ok) {
		url.searchParams.set("dataType", "existsProduct");
		let product = await response.json();
		fillForm(product);
	} else {
		throw new Error("Не удалось сохранить продукт. " + response.status);
	}
}

addPriceButton.onclick = loadNewPrice;
removePriceButton.onclick = function() {
	if(selectedProductPriceContainer != null) {
		selectedProductPriceContainer.parentNode.removeChild(selectedProductPriceContainer);
	}
};

addCaloriesButton.onclick = loadNewCalories;
removeCaloriesButton.onclick = function() {
	if(selectedProductCaloriesContainer != null) {
		selectedProductCaloriesContainer.parentNode.removeChild(selectedProductCaloriesContainer);
	}
};



async function loadProduct() {
	url.searchParams.set("newTab", "false");
	let response = await fetch(url);
	
	if(response.ok) {
		let product = await response.json();
		url.searchParams.set("productId", product.ID);
		fillForm(product);
	} else {
		throw new Error("Не удалось загрузить продукт. " + response.status);
	}
}

async function loadNewPrice() {
	url.searchParams.set("dataType", "newProductPrice");
	let response = await fetch(url);
	
	if(response.ok) {
		let productPrice = await response.json();
		productPricesList.append(createProductPriceElement(productPrice));
	} else {
		throw new Error("Не удалось создать новую цену для продукта." + response.status);
	}
}

async function loadNewCalories() {
	url.searchParams.set("dataType", "newProductCalories");
	let response = await fetch(url);
	
	if(response.ok) {
		let productCalories = await response.json();
		productCaloriesList.append(createProductCaloriesElement(productCalories));
	} else {
		throw new Error("Не удалось создать новую каллорийность для продукта." + response.status);
	}
}

async function loadNewTag(tagsContainer) {
	url.searchParams.set("dataType", "newProductTag");
	let response = await fetch(url);
	
	if(response.ok) {
		let productTag = await response.json();
		tagsContainer.append(createProductTagElement(productTag));
	} else {
		throw new Error("Не удалось создать новый тег." + response.status);
	}
}


function formDataToProduct() {
	let product = {};
	
	product.ID = url.searchParams.get("productId");
	product.NAME = productNameInput.value;
	product.UNIT = productUnitInput.value;
	
	product.PRICES = [];
	for(let productPriceContainer of productPricesList.children) {
		let productPrice = {};
		productPrice.ID = productPriceContainer.PRICE_ID;
		productPrice.PRICE = productPriceContainer.getElementsByClassName("productPriceInput")[0].value;
		productPrice.TAGS = [];
		
		let tagsContainer = productPriceContainer.getElementsByClassName("productPriceTags")[0];	
		for(let productTagContainer of tagsContainer.children) {
			let productTag = {};
			productTag.ID = productTagContainer.TAG_ID;
			productTag.NAME = productTagContainer.getElementsByClassName("tagName")[0].value;
			productTag.VALUE = productTagContainer.getElementsByClassName("tagValue")[0].value;
			productPrice.TAGS.push(productTag);
		}
		
		product.PRICES.push(productPrice);
	}
	
	product.CALORIES = [];
	for(let productCaloriesContainer of productCaloriesList.children) {
		let productCalories = {};
		productCalories.ID = productCaloriesContainer.CALORIES_ID;
		productCalories.CALORIES = productCaloriesContainer.getElementsByClassName("productCaloriesInput")[0].value;
		productCalories.TAGS = [];
		
		let tagsContainer = productCaloriesContainer.getElementsByClassName("productCaloriesTags")[0];
		for(let productTagContainer of tagsContainer.children) {
			let productTag = {};
			productTag.ID = productTagContainer.TAG_ID;
			productTag.NAME = productTagContainer.getElementsByClassName("tagName")[0].value;
			productTag.VALUE = productTagContainer.getElementsByClassName("tagValue")[0].value;
			productCalories.TAGS.push(productTag);
		}
		
		product.CALORIES.push(productCalories);
	}
	
	return product;
}

function fillForm(product) {
    console.log(product);

	productNameHeader.innerHTML = `Продукт: ${product.NAME}`;
	productNameInput.value = product.NAME;
	productUnitInput.value = product.UNIT;
	productAveragePriceInput.value = product.AVERAGE_PRICE;
	productAverageCaloriesInput.value = product.AVERAGE_CALORIES;
	
	productPricesList.textContent = "";
	for(let productPrice of product.PRICES) {
		let productPriceElement = createProductPriceElement(productPrice);
		
		let productPriceTags = productPriceElement.getElementsByClassName("productPriceTags")[0];
		for(let productTag of productPrice.TAGS) {
			let productPriceTag = createProductTagElement(productTag);
			productPriceTags.append(productPriceTag);
		}
			
		productPricesList.append(productPriceElement);
	}
	
	productCaloriesList.textContent = "";
	for(let productCalories of product.CALORIES) {
		let productCaloriesElement = createProductCaloriesElement(productCalories);
		
		let productCaloriesTags = productCaloriesElement.getElementsByClassName("productCaloriesTags")[0];
		for(let productTag of productCalories.TAGS) {
			let productCaloriesTag = createProductTagElement(productTag);
			productCaloriesTags.append(productCaloriesTag);
		}
		
		productCaloriesList.append(productCaloriesElement);
	}
}


function createProductPriceElement(productPrice) {
	let productPriceContainer = document.createElement("div");
	productPriceContainer.classList.add("productPriceCommonContainer");
	productPriceContainer.onclick = function() {
		selectedProductPriceContainer = productPriceContainer;
	};
	productPriceContainer.innerHTML = 
			`<div class="productPriceTagsContainer">
				<button class="addTagButton">+</button>
				<div class="productPriceTags">
				</div>
			</div>
			<label class="productPriceLabel">Цена <input class="productPriceInput"/></label>`;
	productPriceContainer.getElementsByClassName("productPriceInput")[0].value = productPrice.PRICE;
	productPriceContainer.PRICE_ID = productPrice.ID;
	
	let productPriceTags = productPriceContainer.getElementsByClassName("productPriceTags")[0];
	productPriceContainer.getElementsByClassName("addTagButton")[0].onclick = function() {
		loadNewTag(productPriceTags);
	};
	
	return productPriceContainer;
}

function createProductCaloriesElement(productCalories) {
	let productCaloriesContainer = document.createElement("div");
	productCaloriesContainer.classList.add("productCaloriesCommonContainer");
	productCaloriesContainer.onclick = function() {
		selectedProductCaloriesContainer = productCaloriesContainer;
	};
	productCaloriesContainer.innerHTML = 
			`<div class="productCaloriesTagsContainer">
				<button class="addTagButton">+</button>
				<div class="productCaloriesTags">
				</div>
			</div>
			<label class="productCaloriesLabel">Каллорийность <input class="productCaloriesInput"/></label>`;
	productCaloriesContainer.getElementsByClassName("productCaloriesInput")[0].value = productCalories.CALORIES;
	productCaloriesContainer.CALORIES_ID = productCalories.ID;
	
	let productCaloriesTags = productCaloriesContainer.getElementsByClassName("productCaloriesTags")[0];
	productCaloriesContainer.getElementsByClassName("addTagButton")[0].onclick = function() {
		loadNewTag(productCaloriesTags);
	};
	
	return productCaloriesContainer;
}

function createProductTagElement(productTag) {
	let productTagContainer = document.createElement("div");
	productTagContainer.classList.add("productTagContainer");
	productTagContainer.innerHTML = 
			`<input class="tagName" value="${productTag.NAME}"/>
			<input class="tagValue" value="${productTag.VALUE}"/>
			<button class="removeTagButton">-</button>`;
	productTagContainer.TAG_ID = productTag.ID;
	
	productTagContainer.getElementsByClassName("removeTagButton")[0].onclick = function() {
		productTagContainer.parentNode.removeChild(productTagContainer);
	};
	
	return productTagContainer;
}



loadProduct();