package com.olivia.crawling.crawlers;

import com.olivia.crawling.domain.entities.EcommerceData;
import com.olivia.crawling.domain.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProdutoCrawler {

    private HashSet<String> productLinks;
    private EcommerceData productData;
    private static Logger LOG = LoggerFactory.getLogger(ProdutoCrawler.class);

    public ProdutoCrawler() {
        productLinks = new HashSet<String>();
    }

    public void getProduct(String URL) {
        //4. Check if you have already crawled the URLs
        //(we are intentionally not checking for duplicate content in this example)
        if (!productLinks.contains(URL)) {
            Transaction transaction = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()){
                productData = new EcommerceData();
                transaction = session.beginTransaction();
                //4. (i) If not add it to the index
                if (productLinks.add(URL)) {
                    LOG.info("url de produto adicionado: " + URL);
                }

                //2. Fetch the HTML code
                Document document = Jsoup.connect(URL).get();

                //3. Parse the HTML to extract product data
                Element infoSection = document.getElementById("info-section");

                //Picking up the data from the page
                Element categoriaElement = pickItemCategory(document);
                Double pesoItemDouble = pickItemWeight(infoSection);
                String corItemString = pickItemColor(infoSection);
                Double discountItemDouble = pickItemDiscount(document);
                Float ratingItemFloat = pickItemRating(document);
                Float priceItemFloat = pickItemPrice(document);

                productData.setProdutoNome(document.getElementById("product-name-default").text());
                productData.setUrl(URL);
                productData.setCategoria(categoriaElement.text());
                productData.setPeso(pesoItemDouble);
                productData.setCor(corItemString);
                productData.setDesconto(discountItemDouble);
                productData.setClassificacao(ratingItemFloat);
                productData.setPreco(priceItemFloat);

                session.save(productData);
                LOG.info("Produto salvo: " + productData.getProdutoNome());
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                LOG.error("Erro com a URL: " + URL, e);
            }
        }
    }

    private Element pickItemCategory(Document document) {
        Elements categoriasItem = document.getElementsByClass("product-breadcrumb");
        Iterator<Element> iteratorCategorias = categoriasItem.iterator();
        Element categoriaElement = categoriasItem.get(0);
        while (iteratorCategorias.hasNext()) {
            categoriaElement = iteratorCategorias.next();
        }
        return categoriaElement;
    }

    private Double pickItemWeight(Element infoSection) {
        try {
            Elements elementsPeso = infoSection.getElementsMatchingText("(([pP][eE][sS][oO])|[kK][gG])");
            Double pesoItemDouble = new Double("");
            if (elementsPeso != null || !elementsPeso.isEmpty()) {
                String pesoItemValor = elementsPeso.text();
                Pattern pesoPattern = Pattern.compile("(([0-9]{4})|([0-9]{3})|([0-9]{2}))[,][0-9]{2}");
                Matcher pesoMatcher = pesoPattern.matcher(pesoItemValor);
                if (pesoMatcher.find()) {
                    pesoItemDouble = Double.parseDouble(pesoItemValor.substring(pesoMatcher.start(), pesoMatcher.end()));
                }
            }
            return pesoItemDouble;
        } catch (Exception e) {
            return new Double("");
        }
    }

    private String pickItemColor(Element infoSection) {
        try {
            Elements elementsCor = infoSection.getElementsMatchingText("(([pP][eE][sS][oO])|[kK][gG])");
            String corItemString = new String();
            if (elementsCor != null || !elementsCor.isEmpty()) {
                String corItemText = elementsCor.text();
                Pattern corPattern = Pattern.compile("(([pP][rR][eE][tT][oO])|([aA][zZ][uU][lL])|([vV][eE][rR][mM][eE][lL][hH][oO])|([vV][eE][rR][dD][eE]))");
                Matcher corMatcher = corPattern.matcher(corItemText);
                if (corMatcher.find()) {
                    corItemString = corItemText.substring(corMatcher.start(), corMatcher.end());
                }
            }
            return corItemString;
        } catch (Exception e) {
            return new String();
        }
    }

    private Double pickItemDiscount(Document document) {
        try {
            Elements elementsDiscount = document.getElementsMatchingOwnText("([rR][eE][cC][eE][bB][aA][ ])");
            Double discountItemDouble = new Double("");
            if (elementsDiscount != null || !elementsDiscount.isEmpty()) {
                String discountItemText = elementsDiscount.text();
                Pattern discountPattern = Pattern.compile("(([0-9]{4})|([0-9]{3})|([0-9]{2}))[,][0-9]{2}");
                Matcher discountMatcher = discountPattern.matcher(discountItemText);
                if (discountMatcher.find()) {
                    discountItemDouble = Double.parseDouble(discountItemText.substring(discountMatcher.start(), discountMatcher.end()));
                }
            }
            return discountItemDouble;
        } catch (Exception e) {
            return new Double("");
        }
    }

    private Float pickItemRating(Document document) {
        try {
            Element elementRating = document.getElementById("svg-desc");
            Float ratingItemFloat = new Float("");
            if (elementRating != null || !elementRating.hasText()) {
                String ratingItemText = elementRating.text();
                Pattern ratingPattern = Pattern.compile("([0-9]*[.])?[0-9]+");
                Matcher ratingMatcher = ratingPattern.matcher(ratingItemText);
                if (ratingMatcher.find()) {
                    ratingItemFloat = Float.parseFloat(ratingItemText.substring(ratingMatcher.start(), ratingMatcher.end()));
                }
            }
            return ratingItemFloat;
        } catch (Exception e) {
            return new Float("");
        }
    }

    private Float pickItemPrice(Document document) {
        try {
            Element elementPrice = document.getElementById("btn-buy").parent().parent().parent().parent().previousElementSibling();
            Float priceItemFloat = new Float("");
            if (elementPrice != null || !elementPrice.hasText()) {
                String priceItemText = elementPrice.text();
                Pattern pricePattern = Pattern.compile("[0-9]*[,][0-9]{2}");
                Matcher priceMatcher = pricePattern.matcher(priceItemText);
                if (priceMatcher.find()) {
                    priceItemFloat = Float.parseFloat(priceItemText.substring(priceMatcher.start(), priceMatcher.end()));
                }
            }
            return priceItemFloat;
        } catch (Exception e) {
            return new Float("");
        }
    }

}
