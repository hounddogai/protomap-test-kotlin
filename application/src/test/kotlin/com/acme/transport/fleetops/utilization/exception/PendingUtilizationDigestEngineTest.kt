package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingUtilizationDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingUtilizationDigestEngine(newRegistry())
        val item = newPendingUtilizationDigest("code-976", "label-739")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
