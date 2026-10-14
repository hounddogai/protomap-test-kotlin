package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AccessorialTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAccessorialTemplateEngine(newRegistry())
        val item = newAccessorialTemplate("code-618", "name-9")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
