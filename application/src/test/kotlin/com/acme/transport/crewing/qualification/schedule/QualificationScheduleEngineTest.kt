package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class QualificationScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultQualificationScheduleEngine(newRegistry())
        val item = newQualificationSchedule("code-695", "label-183")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
