package csre.iitb.ac.in.client;

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.control.LayerSwitcher;
import org.gwtopenmaps.openlayers.client.control.MousePosition;
import org.gwtopenmaps.openlayers.client.control.MousePositionOptions;
import org.gwtopenmaps.openlayers.client.control.MousePositionOutput;
import org.gwtopenmaps.openlayers.client.control.OverviewMap;
import org.gwtopenmaps.openlayers.client.control.ScaleLine;
import org.gwtopenmaps.openlayers.client.control.WMSGetFeatureInfo;
import org.gwtopenmaps.openlayers.client.control.WMSGetFeatureInfoOptions;
import org.gwtopenmaps.openlayers.client.event.GetFeatureInfoListener;
import org.gwtopenmaps.openlayers.client.layer.TransitionEffect;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.layer.VectorOptions;
import org.gwtopenmaps.openlayers.client.layer.WMS;
import org.gwtopenmaps.openlayers.client.layer.WMSOptions;
import org.gwtopenmaps.openlayers.client.layer.WMSParams;
import org.gwtopenmaps.openlayers.client.protocol.WFSProtocol;
import org.gwtopenmaps.openlayers.client.protocol.WFSProtocolOptions;
import org.gwtopenmaps.openlayers.client.strategy.BBoxStrategy;
import org.gwtopenmaps.openlayers.client.strategy.RefreshStrategy;
import org.gwtopenmaps.openlayers.client.strategy.Strategy;

import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
//import com.google.gwt.dom.client.NodeList;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.DOMException;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

/** Entry point classes define <code>onModuleLoad()</code>. */
public class GNR629Project implements EntryPoint {
	/** This is the entry point method. */
	MapWidget mapWidget;
	 ListBox wmsLayersList;
	 ListBox wmsOperationsList;
	 FlexTable grid;
	 ListBox wfsOperationsList;
	  ListBox featuresList;
	  
	public void onModuleLoad() {
		
		/******* Create the Map Widget *********************/
		MapOptions defaultMapOptions = new MapOptions();
		defaultMapOptions.setNumZoomLevels(16);
		 mapWidget = new MapWidget("500px", "500px", defaultMapOptions);
		final Map map = mapWidget.getMap();
	
		
		/************* Code for Mouse hover ***************/
		MousePositionOutput mpOut = new MousePositionOutput() {
			@Override
			public String format(LonLat lonLat, Map map) {
				String out = "";
				out += "<b>Longitude: </b> ";
				out += lonLat.lon();
				out += "<b>Latitude</b> ";
				out += lonLat.lat();
				return out;
			}
		};
		MousePositionOptions mpOptions = new MousePositionOptions();
		mpOptions.setFormatOutput(mpOut); // rename to setFormatOutput
		map.addControl(new MousePosition(mpOptions));
		map.addControl(new LayerSwitcher());
		map.addControl(new OverviewMap());
		map.addControl(new ScaleLine());

		/********** Add the base layer **********************/
		WMSParams baseParams = new WMSParams();
		baseParams.setFormat("image/png");
		baseParams.setLayers("basic");
		baseParams.setStyles("");

		WMSOptions baseLayerParams = new WMSOptions();
		baseLayerParams.setUntiled();
		baseLayerParams.setTransitionEffect(TransitionEffect.RESIZE);
		String baseUrl = "http://vmap0.tiles.osgeo.org/wms/vmap0";
		final WMS baseLayer = new WMS("OpenLayers WMS", baseUrl, baseParams,baseLayerParams);
		map.addLayer(baseLayer);
		
		/*********** Code for the Dock Panel *****************/
		DockPanel dock = new DockPanel();
		dock.setStyleName("cw-DockPanel");
		//dock.setSpacing(10);
		dock.setHorizontalAlignment(DockPanel.ALIGN_CENTER);
		
		dock.add(new HTML("GNR629 Project: Forest Fire Monitoring"), DockPanel.NORTH);
		dock.add(mapWidget, DockPanel.EAST);
		// Return the content
		dock.ensureDebugId("cwDockPanel");
		map.setCenter(new LonLat(-102.9776611328, 36.8381347), 4);		
		//Code for the Tab Panel //
	    TabPanel tp = new TabPanel();
		tp.add(wmsWidget(map), "Web Map Service");
		tp.add(wfsWidget(map), "Web Feature Service");
		tp.add(wcsWidget(map), "Web Coverage Service");

		
		// Show the 'bar' tab initially.
		tp.selectTab(0);
		// add tab panel to the dock panel
		dock.add(tp, DockPanel.NORTH);
		//dock.setCellHeight(tp, "100px");
		// Add dock panel to the root panel.
		RootLayoutPanel.get().add(dock);
	}
	

	
	public Widget wmsWidget(final Map map){
		 grid = new FlexTable();
		final ListBox urlListBox = new ListBox(){
		    @Override
	        public void setSelectedIndex(int index) {
	            super.setSelectedIndex(index);
	            onChangeBodyWFS(this);
	        }
		};
		urlListBox.addItem("http://localhost:8080/geoserver/wms");
		urlListBox.addItem("http://neowms.sci.gsfc.nasa.gov/wms/wms");
		urlListBox.addChangeHandler(new ChangeHandler() {

	        @Override
	        public void onChange(ChangeEvent event) {
	            onChangeBodyWMS(urlListBox);
	        }
	    });
		final TextBox minX = new TextBox();
		final TextBox minY = new TextBox();
		final TextBox maxX = new TextBox();
		final TextBox maxY = new TextBox();
		Bounds bWMS = map.getExtent();
		minX.setText(String.valueOf(bWMS.getLowerLeftX()));
		minY.setText(String.valueOf(bWMS.getLowerLeftY()));
		maxX.setText(String.valueOf(bWMS.getUpperRightX()));
		maxY.setText(String.valueOf(bWMS.getUpperRightY()));
		wmsLayersList = new ListBox();
		wmsOperationsList = new ListBox();
		final Button submit = new Button();
		submit.setSize("100px", "40px");
		submit.setText("Submit");
		grid.setBorderWidth(0);
		grid.setHTML(0,0,"URL:");
		grid.setWidget(0, 1, urlListBox);
		grid.setHTML(1,0,"Layers");
		grid.setWidget(1, 1, wmsLayersList);
		grid.setHTML(2,0,"Operations");
		grid.setWidget(2, 1, wmsOperationsList);
		grid.setHTML(3,0,"MinX");
		grid.setHTML(3,1,"MinY");
		grid.setWidget(4,0,minX);
		grid.setWidget(4,1,minY);
		grid.setHTML(5,0,"MaxX");
		grid.setHTML(5,1,"MaxY");
		grid.setWidget(6,0,maxX);
		grid.setWidget(6,1,maxY);
		grid.setWidget(7, 1,submit);
		
		// create the panel
	    AbsolutePanel wmsPanel = new AbsolutePanel();
	    wmsPanel.setSize("500px", "430px");
	    wmsPanel.add(grid, 20, 20);
	    
		/********** Code for GetFeatureInfo *****************/
		//Adds the WMSGetFeatureInfo control
	    /*
        final WMSGetFeatureInfoOptions wmsGetFeatureInfoOptions = new WMSGetFeatureInfoOptions();
        wmsGetFeatureInfoOptions.setMaxFeaturess(50);
        wmsGetFeatureInfoOptions.setDrillDown(true);
        final WMSGetFeatureInfo wmsGetFeatureInfo = new WMSGetFeatureInfo(wmsGetFeatureInfoOptions);
	    */
	    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "http://localhost:8080/geoserver/wms?request=getCapabilities");//"http://localhost:8080/geoserver/wms?request=getCapabilities");
		//"http://neowms.sci.gsfc.nasa.gov/wms?request=getCapabilities"
		try {
	      builder.sendRequest(null,new RequestCallback() {

	        public void onError(Request request, Throwable exception) {
	        	//Window.alert("Error Occured while sending requset");
	        }

	        public void onResponseReceived(Request request, Response response) {
	            if (200 == response.getStatusCode()) {
	                // Process the response in response.getText()
					String xmlResponse = response.getText();
					try {
					    // parse the XML document into a DOM
					    Document messageDom = XMLParser.parse(xmlResponse);
					    // populate the list with requests name
					    Node r = messageDom.getElementsByTagName("Request").item(0);
					    NodeList requests = (NodeList)r.getChildNodes();
					    for(int i=0;i<requests.getLength();i++){
					    	if(requests.item(i).getNodeType() == Node.ELEMENT_NODE){
						    	wmsOperationsList.addItem(requests.item(i).getNodeName());	
					    	}
					    }
					    // populate the list with the layers name
					    NodeList layers = messageDom.getElementsByTagName("Layer");
					    for(int i=1;i<layers.getLength();i++){
					    	Node layerNameNode = ((Element)layers.item(i)).getElementsByTagName("Name").item(0);
					    	String layerName = layerNameNode.getFirstChild().getNodeValue();
					    	wmsLayersList.addItem(layerName);
					    }
					    
					    submit.addClickHandler(new ClickHandler(){
							@Override
							public void onClick(ClickEvent event) {
								Bounds bWMS = new Bounds(Double.parseDouble(minX.getValue()),Double.parseDouble(minY.getValue()),Double.parseDouble(maxX.getValue()),Double.parseDouble(maxY.getValue()));
								map.zoomToExtent(bWMS);
								
								int itemIndex = wmsOperationsList.getSelectedIndex();
								if(wmsOperationsList.getItemText(itemIndex).contains("GetCapabilities")){
									
								}else if(wmsOperationsList.getItemText(itemIndex).contains("GetMap")){
									WMSParams wmsParams = new WMSParams();
									wmsParams.setFormat("image/png");
									String layerName = wmsLayersList.getItemText(wmsLayersList.getSelectedIndex());
									wmsParams.setLayers(layerName);
									wmsParams.setTransparent(true);
									wmsParams.setStyles("");
									
									//float minx = Float.parseFloat(minX.getText()); 
									//float miny = Float.parseFloat(minY.getText());
									//float maxx = Float.parseFloat(maxX.getText());
									//float maxy = Float.parseFloat(maxY.getText());
									//Bounds bBox = new Bounds(minx,miny,maxx,maxy);
									
									WMSOptions wmsLayerParams = new WMSOptions();
									wmsLayerParams.setUntiled();
									//wmsLayerParams.setTransitionEffect(TransitionEffect.RESIZE);
									wmsLayerParams.setDisplayOutsideMaxExtent(true);
									wmsLayerParams.setIsBaseLayer(false);
									wmsLayerParams.setLayerOpacity(1.0);
									//wmsLayerParams.setMaxExtent(bBox);
									wmsLayerParams.setProjection(map.getBaseLayer().getProjection().toString());
									String wmsUrl = urlListBox.getItemText(urlListBox.getSelectedIndex());
									WMS wmsLayer = new WMS(layerName, wmsUrl, wmsParams,wmsLayerParams);

									map.addLayer(wmsLayer);	

									//wmsGetFeatureInfoOptions.setLayers(new WMS[]{wmsLayer});
								} else if(wmsOperationsList.getItemText(itemIndex).contains("GetFeatureInfo")){						       
							        
									WMSParams wmsParams = new WMSParams();
									wmsParams.setFormat("image/png");
									String layerName = wmsLayersList.getItemText(wmsLayersList.getSelectedIndex());
									wmsParams.setLayers(layerName);
									wmsParams.setTransparent(true);
									wmsParams.setStyles("");
									
									//float minx = Float.parseFloat(minX.getText()); 
									//float miny = Float.parseFloat(minY.getText());
									//float maxx = Float.parseFloat(maxX.getText());
									//float maxy = Float.parseFloat(maxY.getText());
									//Bounds bBox = new Bounds(minx,miny,maxx,maxy);
									
									WMSOptions wmsLayerParams = new WMSOptions();
									wmsLayerParams.setUntiled();
									//wmsLayerParams.setTransitionEffect(TransitionEffect.RESIZE);
									wmsLayerParams.setDisplayOutsideMaxExtent(true);
									wmsLayerParams.setIsBaseLayer(false);
									wmsLayerParams.setLayerOpacity(1.0);
									//wmsLayerParams.setMaxExtent(bBox);
									wmsLayerParams.setProjection(map.getBaseLayer().getProjection().toString());
									String wmsUrl = urlListBox.getItemText(urlListBox.getSelectedIndex());
									WMS wmsLayer = new WMS(layerName, wmsUrl, wmsParams,wmsLayerParams);
									map.addLayer(wmsLayer);
									
									
									 WMSGetFeatureInfoOptions wmsGetFeatureInfoOptions = new WMSGetFeatureInfoOptions();
								        wmsGetFeatureInfoOptions.setMaxFeaturess(50);
								        wmsGetFeatureInfoOptions.setLayers(new WMS[]{wmsLayer});
								        wmsGetFeatureInfoOptions.setDrillDown(true);
								        //to request a GML string instead of HTML : wmsGetFeatureInfoOptions.setInfoFormat(GetFeatureInfoFormat.GML.toString());
								 
								        WMSGetFeatureInfo wmsGetFeatureInfo = new WMSGetFeatureInfo(
								                wmsGetFeatureInfoOptions);
								 
								        wmsGetFeatureInfo.addGetFeatureListener(new GetFeatureInfoListener() {
								            public void onGetFeatureInfo(GetFeatureInfoEvent eventObject) {
								                //if you did a wmsGetFeatureInfoOptions.setInfoFormat(GetFeatureInfoFormat.GML.toString()) you can do a VectorFeature[] features = eventObject.getFeatures(); here
								            	DialogBoxWithCloseButton db = new DialogBoxWithCloseButton();						                HTML html = new HTML(eventObject.getText());
								                db.setWidget(html);
								                db.center();
								            }
								        });
								        map.addControl(wmsGetFeatureInfo);
								        wmsGetFeatureInfo.activate();
								        mapWidget.getElement().getFirstChildElement().getStyle().setZIndex(0);
								        wmsGetFeatureInfoOptions.setDrillDown(false);
									
									
									
									
									
									
									/*
									wmsGetFeatureInfo.addGetFeatureListener(new GetFeatureInfoListener() {
							            public void onGetFeatureInfo(GetFeatureInfoEvent eventObject) {
							            	wmsGetFeatureInfoOptions.setInfoFormat("text/xml");
							            	//if you did a wmsGetFeatureInfoOptions.setInfoFormat(GetFeatureInfoFormat.GML.toString()) you can do a VectorFeature[] features = eventObject.getFeatures(); here
							            	DialogBox featureInfoDialog = createDialogbox();
							            	
							            	featureInfoDialog.setText(eventObject.toString());
							            	//HTML html = new HTML(eventObject.getText());
							            	//featureInfoDialog.setWidget(html);
							            	featureInfoDialog.show();
							            }
							        });
							        map.addControl(wmsGetFeatureInfo);
							        wmsGetFeatureInfo.activate();
							        */
								}
							}
					    });

					  } catch (DOMException e) {
						  System.out.println("Could not parse XML document.");
					  }				
	            } else {
	              // Handle the error.  Can get the status text from response.getStatusText()
	            	//resp.setText("Error occured"+response.getStatusCode());
	            }
	        }
	      });
	    } catch (RequestException e) {
	      System.out.println("Failed to send the request: " + e.getMessage());
	    }		
	    return wmsPanel;
	}
	
	public Widget wcsWidget(final Map map){
		FlexTable grid = new FlexTable();
		final ListBox urlListBox = new ListBox();
		urlListBox.addItem("http://localhost:8080/geoserver/wcs");
		final TextBox minX = new TextBox();
		final TextBox minY = new TextBox();
		final TextBox maxX = new TextBox();
		final TextBox maxY = new TextBox();
		Bounds bWMS = map.getExtent();
		minX.setText(String.valueOf(bWMS.getLowerLeftX()));
		minY.setText(String.valueOf(bWMS.getLowerLeftY()));
		maxX.setText(String.valueOf(bWMS.getUpperRightX()));
		maxY.setText(String.valueOf(bWMS.getUpperRightY()));
		final ListBox coveragesList = new ListBox();
		final ListBox wcsOperationsList = new ListBox();
		final Button submit = new Button();
		submit.setSize("100px", "40px");
		submit.setText("Submit");
		grid.setBorderWidth(0);
		grid.setHTML(0,0,"URL:");
		grid.setWidget(0, 1, urlListBox);
		grid.setHTML(1,0,"Layers");
		grid.setWidget(1, 1, coveragesList);
		grid.setHTML(2,0,"Operations");
		grid.setWidget(2, 1, wcsOperationsList);
		grid.setHTML(3,0,"MinX");
		grid.setHTML(3,1,"MinY");
		grid.setWidget(4,0,minX);
		grid.setWidget(4,1,minY);
		grid.setHTML(5,0,"MaxX");
		grid.setHTML(5,1,"MaxY");
		grid.setWidget(6,0,maxX);
		grid.setWidget(6,1,maxY);
		grid.setWidget(7, 1,submit);
		
		// create the panel
	    AbsolutePanel wcsPanel = new AbsolutePanel();
	    wcsPanel.setSize("500px", "430px");
	    wcsPanel.add(grid, 20, 20);
	    
	    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "http://localhost:8080/geoserver/wcs?request=GetCapabilities");
		try {
	      builder.sendRequest(null,new RequestCallback() {

	        public void onError(Request request, Throwable exception) {
	        	//Window.alert("Error Occured while sending requset");
	        }

	        public void onResponseReceived(Request request, Response response) {
	            if (200 == response.getStatusCode()) {
	                // Process the response in response.getText()
					String xmlResponse = response.getText();
					try {
					    // parse the XML document into a DOM
					    Document messageDom = XMLParser.parse(xmlResponse);
					    // populate the list with requests name
					    NodeList requests = messageDom.getElementsByTagName("Operation");
					    for(int i=0;i<requests.getLength();i++){
					    	if(requests.item(i).getNodeType() == Node.ELEMENT_NODE){
						    	wcsOperationsList.addItem(((Element)requests.item(i)).getAttribute("name"));	
					    	}
					    }
					    // populate the list with the layers name
					    NodeList coverages = messageDom.getElementsByTagName("CoverageSummary");
					    for(int i=0;i<coverages.getLength();i++){
					    	Node coverageIdNode = ((Element)coverages.item(i)).getElementsByTagName("CoverageId").item(0);
					    	String coverageName = coverageIdNode.getFirstChild().getNodeValue();
					    	coveragesList.addItem(coverageName);
					    }
					    
					    submit.addClickHandler(new ClickHandler(){
					    	
					    	
							@Override
							public void onClick(ClickEvent event) {
								
								Bounds bWMS = new Bounds(Double.parseDouble(minX.getValue()),Double.parseDouble(minY.getValue()),Double.parseDouble(maxX.getValue()),Double.parseDouble(maxY.getValue()));
								map.zoomToExtent(bWMS);
								
								int itemIndex = wcsOperationsList.getSelectedIndex();
								if(wcsOperationsList.getItemText(itemIndex).contains("GetCapabilities")){
									
								}else if(wcsOperationsList.getItemText(itemIndex).contains("DescribeCoverage")){
									WMSParams wmsParams = new WMSParams();
									wmsParams.setFormat("image/png");
									String layerName = coveragesList.getItemText(coveragesList.getSelectedIndex());
									wmsParams.setLayers(layerName);
									wmsParams.setTransparent(true);
									wmsParams.setStyles("");
									
									WMSOptions wmsLayerParams = new WMSOptions();
									wmsLayerParams.setUntiled();
									//wmsLayerParams.setTransitionEffect(TransitionEffect.RESIZE);
									wmsLayerParams.setDisplayOutsideMaxExtent(true);
									wmsLayerParams.setIsBaseLayer(false);
									wmsLayerParams.setLayerOpacity(1.0);
									//wmsLayerParams.setMaxExtent(bBox);
									wmsLayerParams.setProjection(map.getBaseLayer().getProjection().toString());
									String wcsUrl = urlListBox.getItemText(urlListBox.getSelectedIndex());
									WMS wmsLayer = new WMS(layerName, wcsUrl, wmsParams,wmsLayerParams);
									map.addLayer(wmsLayer);
								} else if(wcsOperationsList.getItemText(itemIndex).contains("GetCoverage")){						       

								}
							}
					    });

					  } catch (DOMException e) {
						  System.out.println("Could not parse XML document.");
					  }				
	            } else {
	              // Handle the error.  Can get the status text from response.getStatusText()
	            }
	        }
	      });
	    } catch (RequestException e) {
	      System.out.println("Failed to send the request: " + e.getMessage());
	    }
	   
	    return wcsPanel;
	}
	
	
	
	public Widget wfsWidget(final Map map){
		FlexTable grid = new FlexTable();
		final ListBox urlListBox = new ListBox(){
			@Override
		        public void setSelectedIndex(int index) {
            super.setSelectedIndex(index);
            onChangeBodyWFS(this);
        }
	};
		urlListBox.addItem("http://localhost:8080/geoserver/wfs");
		urlListBox.addItem("http://demo.opengeo.org/geoserver/wfs");
		
		urlListBox.addChangeHandler(new ChangeHandler() {

	        @Override
	        public void onChange(ChangeEvent event) {
	            onChangeBodyWFS(urlListBox);
	        }
	    });
		final TextBox minX = new TextBox();
		final TextBox minY = new TextBox();
		final TextBox maxX = new TextBox();
		final TextBox maxY = new TextBox();
		Bounds bWMS = map.getExtent();
		minX.setText(String.valueOf(bWMS.getLowerLeftX()));
		minY.setText(String.valueOf(bWMS.getLowerLeftY()));
		maxX.setText(String.valueOf(bWMS.getUpperRightX()));
		maxY.setText(String.valueOf(bWMS.getUpperRightY()));
		featuresList = new ListBox();
		wfsOperationsList = new ListBox();
		final Button submit = new Button();
		submit.setSize("100px", "40px");
		submit.setText("Submit");
		grid.setBorderWidth(0);
		grid.setHTML(0,0,"URL:");
		grid.setWidget(0, 1, urlListBox);
		grid.setHTML(1,0,"Features");
		grid.setWidget(1, 1, featuresList);
		grid.setHTML(2,0,"Operations");
		grid.setWidget(2, 1, wfsOperationsList);
		grid.setHTML(3,0,"MinX");
		grid.setHTML(3,1,"MinY");
		grid.setWidget(4,0,minX);
		grid.setWidget(4,1,minY);
		grid.setHTML(5,0,"MaxX");
		grid.setHTML(5,1,"MaxY");
		grid.setWidget(6,0,maxX);
		grid.setWidget(6,1,maxY);
		grid.setWidget(7, 1,submit);
		
		// create the panel
	    AbsolutePanel wfsPanel = new AbsolutePanel();
	    wfsPanel.setSize("500px", "430px");
	    wfsPanel.add(grid, 20, 20);
	    
	    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "http://localhost:8080/geoserver/wfs?request=GetCapabilities");
		try {
	      builder.sendRequest(null,new RequestCallback() {
	
	        public void onError(Request request, Throwable exception) {
	        	//Window.alert("Error Occured while sending requset");
	        }
	
	        public void onResponseReceived(Request request, Response response) {
	            if (200 == response.getStatusCode()) {
	                // Process the response in response.getText()
					String xmlResponse = response.getText();
					try {
					    // parse the XML document into a DOM
					    Document messageDom = XMLParser.parse(xmlResponse);
					    // populate the list with requests name
					    NodeList requests = messageDom.getElementsByTagName("Operation");
					    for(int i=0;i<requests.getLength();i++){
					    	if(requests.item(i).getNodeType() == Node.ELEMENT_NODE){
						    	wfsOperationsList.addItem(((Element)requests.item(i)).getAttribute("name"));	
					    	}
					    }
					    // populate the list with the layers name
					    NodeList features = messageDom.getElementsByTagName("FeatureType");
					    for(int i=0;i<features.getLength();i++){
					    	Node featureNode = ((Element)features.item(i)).getElementsByTagName("Name").item(0);
					    	String featureName = featureNode.getFirstChild().getNodeValue();
					    	featuresList.addItem(featureName);
					    }
					    
					    submit.addClickHandler(new ClickHandler(){
							@Override
							public void onClick(ClickEvent event) {
								
								Bounds bWMS = new Bounds(Double.parseDouble(minX.getValue()),Double.parseDouble(minY.getValue()),Double.parseDouble(maxX.getValue()),Double.parseDouble(maxY.getValue()));
								map.zoomToExtent(bWMS);
								int itemIndex = wfsOperationsList.getSelectedIndex();
								if(wfsOperationsList.getItemText(itemIndex).contains("GetCapabilities")){
									
								}else if(wfsOperationsList.getItemText(itemIndex).contains("DescribeFeatureType")){
									
								} else if(wfsOperationsList.getItemText(itemIndex).contains("GetFeature")){						       
									WFSProtocolOptions wfsProtocolOptions = new WFSProtocolOptions();
									String wfsUrl = urlListBox.getItemText(urlListBox.getSelectedIndex());
									wfsProtocolOptions.setUrl(wfsUrl);
									String featureName = featuresList.getItemText(featuresList.getSelectedIndex());
									wfsProtocolOptions.setFeatureType(featureName.substring(featureName.indexOf(':')+1));
									wfsProtocolOptions.setFeatureNameSpace("http://www.openplans.org/topp");								
									
									WFSProtocol wfsProtocol = new WFSProtocol(wfsProtocolOptions);

									VectorOptions vectorOptions = new VectorOptions();
									vectorOptions.setProtocol(wfsProtocol);
									vectorOptions.setStrategies(new Strategy[] { new BBoxStrategy()});
									//vectorOptions.setProjection(map.getBaseLayer().getProjection().toString());
									
									Vector wfsLayer = new Vector(featureName, vectorOptions);
									map.addLayer(wfsLayer);
								}
							}
					    });
	
					  } catch (DOMException e) {
						  System.out.println("Could not parse XML document.");
					  }				
	            } else {
	              // Handle the error.  Can get the status text from response.getStatusText()
	            }
	        }
	      });
	    } catch (RequestException e) {
	      System.out.println("Failed to send the request: " + e.getMessage());
	    }
	    
	    return wfsPanel;	
	}
	
	public void onChangeBodyWMS(ListBox lb) {
		System.out.println(lb.getValue(lb.getSelectedIndex())+"?request=getCapabilities");//"http://localhost:8080/geoserver/wms?request=getCapabilities)
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, lb.getValue(lb.getSelectedIndex())+"?request=getCapabilities");//"http://localhost:8080/geoserver/wms?request=getCapabilities");
		//"http://neowms.sci.gsfc.nasa.gov/wms?request=getCapabilities"
		try {
	      builder.sendRequest(null,new RequestCallback() {

	        public void onError(Request request, Throwable exception) {
	        	//Window.alert("Error Occured while sending requset");
	        }

	        public void onResponseReceived(Request request, Response response) {
	            if (200 == response.getStatusCode()) {
	                // Process the response in response.getText()
					String xmlResponse = response.getText();
					try {
					    // parse the XML document into a DOM
					    Document messageDom = XMLParser.parse(xmlResponse);
					    // populate the list with requests name
					    Node r = messageDom.getElementsByTagName("Request").item(0);
					    NodeList requests = (NodeList)r.getChildNodes();
					    wmsOperationsList.clear();
						//grid.setWidget(1, 1, wmsLayersList);
				
					    for(int i=0;i<requests.getLength();i++){
					    	if(requests.item(i).getNodeType() == Node.ELEMENT_NODE){
						    	wmsOperationsList.addItem(requests.item(i).getNodeName());	
					    	}
					    }
					    // populate the list with the layers name
					    NodeList layers = messageDom.getElementsByTagName("Layer");
					    wmsLayersList.clear();
					    for(int i=1;i<layers.getLength();i++){
					    	Node layerNameNode = ((Element)layers.item(i)).getElementsByTagName("Name").item(0);
					    	String layerName = layerNameNode.getFirstChild().getNodeValue();
					    	wmsLayersList.addItem(layerName);
					    }
						//grid.setWidget(2, 1, wmsOperationsList);

	  
	}catch (Exception e) {
	      System.out.println("Failed to send the request: " + e.getMessage());
	    }
	            }
	        }
	      });
		}catch (RequestException e) {
		      System.out.println("Failed to send the request: " + e.getMessage());
		    }		
	}
	
	//WFS Change URL Action 
	public void onChangeBodyWFS(ListBox lb) {
		System.out.println(lb.getValue(lb.getSelectedIndex())+"?request=getCapabilities");//"http://localhost:8080/geoserver/wms?request=getCapabilities)
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, lb.getValue(lb.getSelectedIndex())+"?request=getCapabilities");//"http://localhost:8080/geoserver/wms?request=getCapabilities");
		//"http://neowms.sci.gsfc.nasa.gov/wms?request=getCapabilities"
		try {
	      builder.sendRequest(null,new RequestCallback() {

	        public void onError(Request request, Throwable exception) {
	        	//Window.alert("Error Occured while sending requset");
	        }

	        public void onResponseReceived(Request request, Response response) {
	            if (200 == response.getStatusCode()) {
	                // Process the response in response.getText()
					String xmlResponse = response.getText();
					try {
					    // parse the XML document into a DOM
					    Document messageDom = XMLParser.parse(xmlResponse);
					    // populate the list with requests name
					    NodeList requests = messageDom.getElementsByTagName("Operation");
					    wfsOperationsList.clear();
					    for(int i=0;i<requests.getLength();i++){
					    	if(requests.item(i).getNodeType() == Node.ELEMENT_NODE){
						    	wfsOperationsList.addItem(((Element)requests.item(i)).getAttribute("name"));	
					    	}
					    }
					    // populate the list with the layers name
					    NodeList features = messageDom.getElementsByTagName("FeatureType");
					    featuresList.clear();
					    for(int i=0;i<features.getLength();i++){
					    	Node featureNode = ((Element)features.item(i)).getElementsByTagName("Name").item(0);
					    	String featureName = featureNode.getFirstChild().getNodeValue();
					    	featuresList.addItem(featureName);
					    }
						//grid.setWidget(2, 1, wmsOperationsList);

	  
	}catch (Exception e) {
	      System.out.println("Failed to send the request: " + e.getMessage());
	    }
	            }
	        }
	      });
		}catch (RequestException e) {
		      System.out.println("Failed to send the request: " + e.getMessage());
		    }		
	}
	
}


