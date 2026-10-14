package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultViolationProfileEngine(newRegistry())
        val item = newViolationProfile("code-625", "label-815")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
