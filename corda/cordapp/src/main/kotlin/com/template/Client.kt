package com.template

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.contracts.StateAndRef
import net.corda.core.utilities.NetworkHostAndPort.Companion.parse
import net.corda.core.utilities.loggerFor
import org.slf4j.Logger

/**
 * Demonstration of how to use the CordaRPCClient to connect to a Corda Node and
 * stream the contents of the node's vault.
 */
fun main(args: Array<String>) = TemplateClient().main(args)

private class TemplateClient {
    companion object {
        val logger: Logger = loggerFor<TemplateClient>()
        private fun logState(state: StateAndRef<ADDState>) = logger.info("{}", state.state.data)
    }

    fun main(args: Array<String>) {
        require(args.size == 1) { "Usage: TemplateClient <node address>" }
        val nodeAddress = parse(args[0])
        val client = CordaRPCClient(nodeAddress)

        // Can be amended in the com.template.MainKt file.
        val proxy = client.start("user1", "test").proxy

        // Grab all existing ADDStates and all future ADDStates.
        val (snapshot, updates) = proxy.vaultTrack(ADDState::class.java)

        // Log the existing ADDStates and listen for new ones.
        snapshot.states.forEach { logState(it) }
        updates.toBlocking().subscribe { update ->
            update.produced.forEach { logState(it) }
        }
    }
}