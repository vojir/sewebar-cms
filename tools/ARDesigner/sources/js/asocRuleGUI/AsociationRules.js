/**
 * Class: AsociationRules
 * This is the main class of the Application AsociationRulesGUI. This class creates the
 * structure and manage communication between the other parts.
 */
var AsociationRules = new Class({
    Implements: Events,

    MAIN_DIV_ID: "mainDiv", 

    /**
     * Function: initialize
     * This function creates instance of this class. It sets lenguage of application
     * and locations of necessary resources on the server.
     *
     * Parameters:
     * lang         {String} Language of the application
     * urlGet       {String} Url where the app gets Info
     * urlSet       {String} Url where the app serializes Info
     */
    initialize: function(lang, urlGet, urlSet){
        this.urlSet = urlSet;
        this.lang = lang;

        this.asociationRules = new Array();

        this.availableBooleans = new Array();
        this.availableAttributes = new Array();
        this.availableInterestMeasures = new Array()

        this.language = new LanguageSupport();

        this.getInfo(urlGet);
    },

    /**
     * Function: getInfo
     * This function gets Data and Configuration from server and solve the JSON
     * it gets.
     *
     * Parameters:
     * url     {String} url on the web where the app gets info.
     */
    getInfo: function(url){
        new Request.JSON({
            url: url,
            onComplete: function(item){
                this.lang = item.lang;
                // optional - nyn�j�� stav, prohibited - nezobraz� se displayAskingWindow, required - "" nen� funk�n� �e�en�."
                AsociationRules.imThreshold = item.imThreshold;
                AsociationRules.attrCoef = item.attrCoef;
                LanguageSupport.actualLang = this.lang;

                this.serverInfo = new ServerInfo(item)
                var moreRules = this.serverInfo.getMoreRules();
                if(moreRules == "false"){
                    moreRules = false;
                }
                else{
                    moreRules = true;
                }
                new BasicStructureGUI(this.serverInfo.getBooleans(), this.serverInfo.getAttributes(), this.serverInfo.getOperators(), this.MAIN_DIV_ID, this.lang, moreRules);

                if(moreRules){
                    $("newRule").addEvent('click', function(event){
                        var newRule = new AsociationRule(this.serverInfo);
                        newRule.addEvent("display", function(){
                            this.setDraggability();
                        }.bind(this));
                        newRule.setMaxSize(this.maxSize);
                        this.asociationRules.push(newRule);
                        var newDiv = newRule.display();
                        newDiv.inject($('rightDivPlace'));
                        this.drag.removeDragability();
                        this.drag.createDragability();
                    }.bind(this));
                }

                $("saveRule").addEvent('click', function(event){
                    var wholeJson = new JSONHelp();
                    var rule = null;
                    for(var actualRule = 0; actualRule < this.asociationRules.length; actualRule++){
                        rule = this.asociationRules[actualRule].toJSON();
                        if(rule == null){
                            new Hlaseni(this.language.getName(this.language.INCORRECT_RULE, this.lang));
                            return;
                        }
                        wholeJson["rule"+actualRule] = this.asociationRules[actualRule].toJSON();
                    }
                    wholeJson.rules = actualRule;
                    var jsonString = JSON.encode(wholeJson);
                    $$('.rule').each(function(ele){
                        ele.dispose();
                    });
                    this.saveServer(jsonString);
                }.bind(this));

                this.maxSize = this.solveSize();
                var allRules = this.serverInfo.getExistingRules();
                for(var actualRule = 0; actualRule < allRules.length; actualRule++){
                    allRules[actualRule].setMaxSize(this.maxSize);
                    allRules[actualRule].addEvent("display", function(){
                        this.setDraggability();
                    }.bind(this));
                    var newRuleDiv1 = allRules[actualRule].display();
                    newRuleDiv1.inject($('rightDivPlace'));
                }
                this.asociationRules = this.asociationRules.concat(allRules);

                if(!moreRules && this.asociationRules.length < 1){
                    var newAsociationRule = new AsociationRule(this.serverInfo);
                    newAsociationRule.addEvent("display", function(){
                        this.setDraggability();
                    }.bind(this));
                    this.asociationRules.push(newAsociationRule);
                    var newRuleDiv = newAsociationRule.display();
                    // This should be injected into the left part.
                    newRuleDiv.inject($('rightDivPlace'));
                }


                this.drag = new Dragability(".ARElement",".prvek");
                this.setDraggability();
            }.bind(this)
        }).get();
    },

    /**
     * Function: solveSize
     * It gets max Size of element
     *
     * Returns:
     * {Number} max size
     */
    solveSize: function(){
        // Solve the size of ARElement based on the biggest size.
        var maxSize = 0;
        $$(".prvek").each(function(el){
            var actualSize = el.getSize().y;
            if(actualSize > maxSize){
                maxSize = actualSize;
            }
        });

        return maxSize;
    },

    /**
     * Function: setDraggability
     * This function sets draggability of elements which should be dragged.
     */
    setDraggability: function(){
        this.drag.removeDragability();
        this.drag.createDragability();
    },

    /**
     * Function: saveServer
     * This function is called by save() and it actually sends the data on the
     * server in variable data.
     *
     * Parameters:
     * which  {String} Data that should be sent to the server.
     */
    saveServer: function(which){
        new Request({
            url: this.urlSet,
            onComplete: function(item){
                this.asociationRules = new Array();
                var hlaseni = new Hlaseni(this.language.getName(this.language.EVERYTHING_OK, this.lang));
                hlaseni.addEvent('closehlaseni', function() {
                    this.fireEvent('saved', item);
                }.bind(this));
            }.bind(this)
        }).post({
            'data': which
        });
    }
});

/**
 * Class: JSONHelp
 * This is supportive class for serialization into JSON. Basically it does nothing.
 */
var JSONHelp = new Class({

    });

function Counter(){
    if ( typeof Counter.counter == 'undefined' ) {
        // It has not... perform the initilization
        Counter.counter = 0;
    }
}
Counter.counter = 0;
