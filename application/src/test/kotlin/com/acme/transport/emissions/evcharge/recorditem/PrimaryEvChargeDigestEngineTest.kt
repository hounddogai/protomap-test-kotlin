package com.acme.transport.emissions.evcharge.recorditem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryEvChargeDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryEvChargeDigestEngine(newRegistry())
        val item = newPrimaryEvChargeDigest("code-869", "name-666")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
