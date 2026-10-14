package com.acme.transport.safety.violation.manualviolationquota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class StrictViolationQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictViolationQuotaEngine(newRegistry())
        val item = newStrictViolationQuota("code-265", "title-774")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
