/*
 * ===========================================================================
 *  IBA CZ Confidential
 * 
 *  (c) Copyright IBA CZ 2014 ALL RIGHTS RESERVED
 *  The source code for this program is not published or otherwise
 *  divested of its trade secrets.
 * 
 * ===========================================================================
 */

package cz.muni.fi.mireval.query.topics;

import java.util.List;

/**
 *
 * @author Martin Liska <martin.liska@ibacz.eu>
 */
public class Topic {
    
    private String topicId;
    private List<TopicTerm> formulaKeyword;
    private List<TopicTerm> textKeyword;

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public List<TopicTerm> getFormulaKeyword() {
        return formulaKeyword;
    }

    public void setFormulaKeyword(List<TopicTerm> formulaKeyword) {
        this.formulaKeyword = formulaKeyword;
    }

    public List<TopicTerm> getTextKeyword() {
        return textKeyword;
    }

    public void setTextKeyword(List<TopicTerm> textKeyword) {
        this.textKeyword = textKeyword;
    }
    
}
