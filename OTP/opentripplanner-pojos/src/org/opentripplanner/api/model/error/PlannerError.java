/* This program is free software: you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public License
   as published by the Free Software Foundation, either version 3 of
   the License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.opentripplanner.api.model.error;

import java.util.ArrayList;
import java.util.List;

import org.opentripplanner.api.ws.Message;

/*
 * This represents an error in trip planning.
 *
 */
public class PlannerError {
	
	
    private int    id;

	
    private String msg;

	
    private List<String> missing = new ArrayList<String>();

	
    private boolean noPath = false;

    /** An error where no path has been found, but no points are missing */
    public PlannerError() {
        noPath = true;
    }

    public PlannerError(boolean np) {
        noPath = np;
    }

    public PlannerError(String msg) {
        setMsg(msg);
    }

    public PlannerError(List<String> missing) {
        this.setMissing(missing);
    }

//    public PlannerError(int id, String msg) {
//        this.id  = id;
//        this.msg = msg;
//    }

    public String getMsg() {
        return msg;
    }

//    public void setMsg(String msg) {
//        this.msg = msg;
//    }

    public void setMsg(String msg) {
//        this.msg = msg.get();
//        this.id  = msg.getId();
    	this.msg = msg;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * @param missing the list of point names which cannot be found (from, to, intermediate.n)
     */
    public void setMissing(List<String> missing) {
        this.missing = missing;
    }

    /**
     * @return the list of point names which cannot be found (from, to, intermediate.n)
     */
    public List<String> getMissing() {
        return missing;
    }

    /**
     * @param noPath whether no path has been found
     */
    public void setNoPath(boolean noPath) {
        this.noPath = noPath;
    }

    /**
     * @return whether no path has been found
     */
    public boolean getNoPath() {
        return noPath;
    }
}
