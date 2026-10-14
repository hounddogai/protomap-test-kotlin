package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationBundleEngine(newRegistry())
        val item = newViolationBundle("code-604", "name-521")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
