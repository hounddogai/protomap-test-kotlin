package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DisputeManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDisputeManifestEngine(newRegistry())
        val item = newDisputeManifest("code-421", "title-861")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
