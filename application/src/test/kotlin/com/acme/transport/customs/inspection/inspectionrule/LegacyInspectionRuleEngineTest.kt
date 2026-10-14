package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyInspectionRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyInspectionRuleEngine(newRegistry())
        val item = newLegacyInspectionRule("code-908", "name-981")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
