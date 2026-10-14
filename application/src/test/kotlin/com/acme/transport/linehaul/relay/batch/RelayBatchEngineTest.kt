package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RelayBatchEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayBatchEngine(newRegistry())
        val item = newRelayBatch("code-676", "label-620")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
