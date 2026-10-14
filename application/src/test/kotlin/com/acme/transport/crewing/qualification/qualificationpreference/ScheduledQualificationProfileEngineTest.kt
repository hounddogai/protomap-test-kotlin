package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ScheduledQualificationProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultScheduledQualificationProfileEngine(newRegistry())
        val item = newScheduledQualificationProfile("code-75", "title-40")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
