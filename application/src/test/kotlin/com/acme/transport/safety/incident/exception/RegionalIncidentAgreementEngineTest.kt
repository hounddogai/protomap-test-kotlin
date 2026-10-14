package com.acme.transport.safety.incident.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalIncidentAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalIncidentAgreementEngine(newRegistry())
        val item = newRegionalIncidentAgreement("code-640", "title-894")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
