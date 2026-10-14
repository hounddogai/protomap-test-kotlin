package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientZoneAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientZoneAgreementEngine(newRegistry())
        val item = newLenientZoneAgreement("code-530", "label-18")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
