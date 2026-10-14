package com.acme.transport.terminal.appointment.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment templates by code. */
interface AppointmentTemplateStore {
    fun find(code: String): AppointmentTemplate?
    fun save(item: AppointmentTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentTemplateStore(
    var items: MutableMap<String, AppointmentTemplate>,
    var order: MutableList<String>,
) : AppointmentTemplateStore {
    override fun find(code: String): AppointmentTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentTemplate) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<AppointmentTemplate> {
        val out = mutableListOf<AppointmentTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentTemplateStore(): AppointmentTemplateStore {
    return MemoryAppointmentTemplateStore(mutableMapOf<String, AppointmentTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentTemplateStore(
    var inner: AppointmentTemplateStore,
    var cache: TtlCache<AppointmentTemplate>,
    var hits: Int,
    var misses: Int,
) : AppointmentTemplateStore {
    override fun find(code: String): AppointmentTemplate? {
        val hit = (cache.get(code) as AppointmentTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentTemplate> = inner.list()
}

fun newCachedAppointmentTemplateStore(inner: AppointmentTemplateStore, ttl: Int, clock: Clock): AppointmentTemplateStore {
    return CachedAppointmentTemplateStore(
        inner = inner,
        cache = newTtlCache<AppointmentTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment templates. Empty fields match all. */
data class AppointmentTemplateQuery(
    var source: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentTemplate): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentTemplate>): MutableList<AppointmentTemplate> {
        val matched = filter(items) { candidate: AppointmentTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentTemplates(store: AppointmentTemplateStore, query: AppointmentTemplateQuery): MutableList<AppointmentTemplate> {
    return query.apply(store.list())
}
