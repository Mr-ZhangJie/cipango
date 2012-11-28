// ========================================================================
// Copyright 2008-2012 NEXCOM Systems
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.cipango.server.transaction;

import org.cipango.server.SipConnection;
import org.cipango.server.SipRequest;
import org.cipango.server.SipResponse;

public interface ClientTransactionListener extends TransactionListener
{
    void handleResponse(SipResponse response);
    
    void customizeRequest(SipRequest request, SipConnection connection);
}
