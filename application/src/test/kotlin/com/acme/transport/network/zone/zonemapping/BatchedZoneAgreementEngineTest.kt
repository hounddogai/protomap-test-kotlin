package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BatchedZoneAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBatchedZoneAgreementEngine(newRegistry())
        val item = newBatchedZoneAgreement("code-456", "label-377")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
