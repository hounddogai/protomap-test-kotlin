package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class BackhaulBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultBackhaulBundleEngine(newRegistry())
        val item = newBackhaulBundle("code-541", "label-602")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
