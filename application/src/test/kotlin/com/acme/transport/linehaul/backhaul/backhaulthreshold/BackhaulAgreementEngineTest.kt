package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulAgreementEngine(newRegistry())
        val item = newBackhaulAgreement("code-944", "label-283")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
