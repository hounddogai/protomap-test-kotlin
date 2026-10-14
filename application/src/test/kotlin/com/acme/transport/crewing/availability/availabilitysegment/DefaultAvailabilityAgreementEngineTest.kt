package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultAvailabilityAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultAvailabilityAgreementEngine(newRegistry())
        val item = newDefaultAvailabilityAgreement("code-873", "label-730")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
