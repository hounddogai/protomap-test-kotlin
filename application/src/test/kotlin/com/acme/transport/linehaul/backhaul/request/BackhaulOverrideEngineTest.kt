package com.acme.transport.linehaul.backhaul.request

import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulOverrideEngine()
        val item = newBackhaulOverride("code-987", "name-929")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
