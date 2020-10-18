package com.bakuard.nutritionManager.controller.convert;

import com.bakuard.nutritionManager.model.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class JsonConverter {

    private MathContext mathContext;

    public JsonConverter(MathContext mathContext) {
        this.mathContext = mathContext;
    }

    public String toJson(List<Product> products) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try(JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
            jsonWriter.beginArray();

            for(Product product : products) {
                jsonWriter.beginObject();

                jsonWriter.name("ID").value(product.getId());
                jsonWriter.name("NAME").value(product.getName());
                jsonWriter.name("UNIT").value(product.getUnit());
                jsonWriter.name("AVERAGE_PRICE").value(product.getAveragePrice(mathContext).toPlainString());
                jsonWriter.name("AVERAGE_CALORIES").value(product.getAverageCalories(mathContext).toPlainString());
                tagsToJson("ALL_TAGS", product.getAllTags(), jsonWriter);
                pricesToJson("PRICES", product.getPrices(), jsonWriter);
                caloriesToJson("CALORIES", product.getCalories(), jsonWriter);

                jsonWriter.endObject();
            }

            jsonWriter.endArray();
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    public String toJson(Product product) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try(JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
            jsonWriter.beginObject();

            jsonWriter.name("ID").value(product.getId());
            jsonWriter.name("NAME").value(product.getName());
            jsonWriter.name("UNIT").value(product.getUnit());
            jsonWriter.name("AVERAGE_PRICE").value(product.getAveragePrice(mathContext).toPlainString());
            jsonWriter.name("AVERAGE_CALORIES").value(product.getAverageCalories(mathContext).toPlainString());
            tagsToJson("ALL_TAGS", product.getAllTags(), jsonWriter);
            pricesToJson("PRICES", product.getPrices(), jsonWriter);
            caloriesToJson("CALORIES", product.getCalories(), jsonWriter);

            jsonWriter.endObject();
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    public String toJson(ProductPrice price) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try(JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
            jsonWriter.beginObject();
            jsonWriter.name("ID").value(price.getId());
            jsonWriter.name("PRICE").value(price.getPrice().toPlainString());
            tagsToJson("TAGS", price.getTags(), jsonWriter);
            jsonWriter.endObject();
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    public String toJson(ProductCalories calories) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try(JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
            jsonWriter.beginObject();
            jsonWriter.name("ID").value(calories.getId());
            jsonWriter.name("CALORIES").value(calories.getCalories().toPlainString());
            tagsToJson("TAGS", calories.getTags(), jsonWriter);
            jsonWriter.endObject();
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    public String toJson(ProductTag tag) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try(JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8))) {
            jsonWriter.beginObject();
            jsonWriter.name("ID").value(tag.getId());
            jsonWriter.name("NAME").value(tag.getName());
            jsonWriter.name("VALUE").value(tag.getValue());
            jsonWriter.endObject();
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    public Product toProduct(String productJson) throws IOException {
        Product.Builder builder = new Product.Builder();

        try(JsonReader jsonReader = new JsonReader(new StringReader(productJson))) {
            jsonReader.beginObject();

            while(jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                if("ID".equals(name)) {
                    builder.setId(jsonReader.nextInt());
                } else if("NAME".equals(name)) {
                    builder.setName(jsonReader.nextString());
                } else if("UNIT".equals(name)) {
                    builder.setUnit(jsonReader.nextString());
                } else if("PRICES".equals(name)) {
                    builder.setPrices(toPrices(jsonReader));
                } else if("CALORIES".equals(name)) {
                    builder.setCalories(toCalories(jsonReader));
                } else {
                    jsonReader.skipValue();
                }
            }

            jsonReader.endObject();
        }

        return builder.build();
    }


    private void pricesToJson(String name,
                               ImmutableMap<Integer, ProductPrice> prices,
                               JsonWriter jsonWriter) throws IOException {
        jsonWriter.name(name).beginArray();
        for(ImmutableMap.Node<Integer, ProductPrice> node : prices) {
            jsonWriter.beginObject();
            jsonWriter.name("ID").value(node.getValue().getId());
            jsonWriter.name("PRICE").value(node.getValue().getPrice().toPlainString());
            tagsToJson("TAGS", node.getValue().getTags(), jsonWriter);
            jsonWriter.endObject();
        }
        jsonWriter.endArray();
    }

    private void caloriesToJson(String name,
                                ImmutableMap<Integer, ProductCalories> calories,
                                JsonWriter jsonWriter) throws IOException {
        jsonWriter.name(name).beginArray();
        for(ImmutableMap.Node<Integer, ProductCalories> node : calories) {
            jsonWriter.beginObject();
            jsonWriter.name("ID").value(node.getValue().getId());
            jsonWriter.name("CALORIES").value(node.getValue().getCalories().toPlainString());
            tagsToJson("TAGS", node.getValue().getTags(), jsonWriter);
            jsonWriter.endObject();
        }
        jsonWriter.endArray();
    }

    private void tagsToJson(String name,
                            ImmutableMap<Integer, ProductTag> tags,
                            JsonWriter jsonWriter) throws IOException {
        jsonWriter.name(name).beginArray();
        for(ImmutableMap.Node<Integer, ProductTag> node : tags)  {
            jsonWriter.beginObject();
            jsonWriter.name("ID").value(node.getValue().getId());
            jsonWriter.name("NAME").value(node.getValue().getName());
            jsonWriter.name("VALUE").value(node.getValue().getValue());
            jsonWriter.endObject();
        }
        jsonWriter.endArray();
    }

    private HashMap<Integer, ProductPrice> toPrices(JsonReader jsonReader) throws IOException {
        HashMap<Integer, ProductPrice> prices = new HashMap<>();

        jsonReader.beginArray();
        while(jsonReader.hasNext()) {
            ProductPrice.Builder builder = new ProductPrice.Builder();

            jsonReader.beginObject();
            while(jsonReader.hasNext()) {
                String name = jsonReader.nextName();

                if("ID".equals(name)) {
                    builder.setId(jsonReader.nextInt());
                } else if("PRICE".equals(name)) {
                    builder.setPrice(new BigDecimal(jsonReader.nextString()));
                } else if("TAGS".equals(name)) {
                    builder.setTags(toTags(jsonReader));
                }
            }
            jsonReader.endObject();

            prices.put(builder.getId(), builder.build());
        }
        jsonReader.endArray();

        return prices;
    }

    private HashMap<Integer, ProductCalories> toCalories(JsonReader jsonReader) throws IOException {
        HashMap<Integer, ProductCalories> calories = new HashMap<>();

        jsonReader.beginArray();
        while(jsonReader.hasNext()) {
            ProductCalories.Builder builder = new ProductCalories.Builder();

            jsonReader.beginObject();
            while(jsonReader.hasNext()) {
                String name = jsonReader.nextName();

                if("ID".equals(name)) {
                    builder.setId(jsonReader.nextInt());
                } else if("CALORIES".equals(name)) {
                    builder.setCalories(new BigDecimal(jsonReader.nextString()));
                } else if("TAGS".equals(name)) {
                    builder.setTags(toTags(jsonReader));
                }
            }
            jsonReader.endObject();

            calories.put(builder.getId(), builder.build());
        }
        jsonReader.endArray();

        return calories;
    }

    private HashMap<Integer, ProductTag> toTags(JsonReader jsonReader) throws IOException {
        HashMap<Integer, ProductTag> prices = new HashMap<>();

        jsonReader.beginArray();
        while(jsonReader.hasNext()) {
            int id = 0;
            String tagName = null;
            String tagValue = null;

            jsonReader.beginObject();
            while(jsonReader.hasNext()) {
                String name = jsonReader.nextName();

                if("ID".equals(name)) {
                    id = jsonReader.nextInt();
                } else if("NAME".equals(name)) {
                    tagName = jsonReader.nextString();
                } else if("VALUE".equals(name)) {
                    tagValue = jsonReader.nextString();
                }
            }
            jsonReader.endObject();

            prices.put(id, new ProductTag(id, tagName, tagValue));
        }
        jsonReader.endArray();

        return prices;
    }

}
