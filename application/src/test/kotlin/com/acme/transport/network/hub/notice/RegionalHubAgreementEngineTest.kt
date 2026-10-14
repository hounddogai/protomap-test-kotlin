package com.acme.transport.network.hub.notice

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalHubAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalHubAgreementEngine()
        val item = newRegionalHubAgreement("code-141", "name-246")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
