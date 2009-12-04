/**
 * Copyright (c) 2009, Barthelemy Dagenais All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * - The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package py4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Responsible for parsing a call command and calling the method on the target
 * object.
 * </p>
 * <p>
 * Currently, the call command assumes that a command is well-formed and that
 * there is no communication problem (e.g., the source virtual machine
 * disconnected in the middle of a command). This is a reasonnable assumption
 * because the two virtual machines are assumed to be on the same host.
 * </p>
 * <p>
 * <b>TODO:</b> Make the call command more robust to communication errors and
 * ill-formed protocol.
 * </p>
 * 
 * @author Barthelemy Dagenais
 * 
 */
public class CallCommand implements Command {

	private Gateway gateway;

	@Override
	public void execute(String command, BufferedReader reader,
			BufferedWriter writer) throws Py4JException, IOException {
		String targetObjectId = reader.readLine();
		String methodName = reader.readLine();
		List<Argument> arguments = new ArrayList<Argument>();
		String line = reader.readLine();

		while (!Protocol.isEmpty(line) && !Protocol.isEnd(line)) {
			Argument argument = new Argument(Protocol.getObject(line), Protocol
					.isReference(line));
			arguments.add(argument);
		}
		ReturnObject returnObject = null;
		try {
			returnObject = gateway.invoke(methodName,
					targetObjectId, arguments);
		} catch (Exception e) {
			e.printStackTrace();
			returnObject = ReturnObject.getErrorReturnObject();
		}
		
		String returnCommand = Protocol.getOutputCommand(returnObject);
		writer.write(returnCommand);
	}

	@Override
	public void init(Gateway gateway) {
		this.gateway = gateway;
	}

}
