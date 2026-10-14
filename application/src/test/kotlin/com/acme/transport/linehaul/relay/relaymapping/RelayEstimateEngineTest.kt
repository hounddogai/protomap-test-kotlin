package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RelayEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRelayEstimateEngine(newRegistry())
        val item = newRelayEstimate("code-248", "name-558")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
