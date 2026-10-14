package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingProofOfDeliveryDraftEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingProofOfDeliveryDraftEngine(newRegistry())
        val item = newRollingProofOfDeliveryDraft("code-831", "name-503")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
