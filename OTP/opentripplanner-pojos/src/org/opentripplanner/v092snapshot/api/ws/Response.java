/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.v092snapshot.api.ws;

import java.util.HashMap;

import org.opentripplanner.api.model.error.PlannerError;
import org.opentripplanner.v092snapshot.api.model.TripPlan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Represents a trip planner response, will be serialized into XML or JSON by Jersey */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {

    private HashMap<String, String> requestParameters;
    private TripPlan plan;
    private PlannerError error = null;

    public Response() {
    }

    // note order the getters below is semi-important, in that that's the order printed by jersey in the return
    // e.g., from a human readable standpoint, it's tradition to have request params, followed by plan, followed by errors

    /**
     * A dictionary of the parameters provided in the request that triggered this response.
     */
    public HashMap<String, String> getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(HashMap<String, String> requestParameters) {
        this.requestParameters = requestParameters;
    }

    /**
     * The actual trip plan.
     */
    public TripPlan getPlan() {
        return plan;
    }

    public void setPlan(TripPlan plan) {
        this.plan = plan;
    }

    /**
     * The error (if any) that this response raised.
     */
    public PlannerError getError() {
        return error;
    }

    public void setError(PlannerError error) {
        this.error = error;
    }
}