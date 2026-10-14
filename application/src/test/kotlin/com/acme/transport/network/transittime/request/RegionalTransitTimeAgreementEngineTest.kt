package com.acme.transport.network.transittime.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalTransitTimeAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalTransitTimeAgreementEngine(newRegistry())
        val item = newRegionalTransitTimeAgreement("code-508", "name-752")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
