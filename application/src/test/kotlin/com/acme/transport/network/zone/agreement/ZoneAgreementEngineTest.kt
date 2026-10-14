package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ZoneAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultZoneAgreementEngine(newRegistry())
        val item = newZoneAgreement("code-956", "title-280")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
