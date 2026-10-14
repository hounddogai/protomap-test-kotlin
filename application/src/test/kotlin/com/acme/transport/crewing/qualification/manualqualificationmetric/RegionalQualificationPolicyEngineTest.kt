package com.acme.transport.crewing.qualification.manualqualificationmetric

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalQualificationPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalQualificationPolicyEngine(newRegistry())
        val item = newRegionalQualificationPolicy("code-405", "name-537")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
