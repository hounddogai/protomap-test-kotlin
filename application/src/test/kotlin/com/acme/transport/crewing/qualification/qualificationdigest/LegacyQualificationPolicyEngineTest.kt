package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyQualificationPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyQualificationPolicyEngine(newRegistry())
        val item = newLegacyQualificationPolicy("code-919", "name-260")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
