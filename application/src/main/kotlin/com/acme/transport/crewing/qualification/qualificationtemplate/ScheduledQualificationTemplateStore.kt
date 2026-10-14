package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled qualification templates by code. */
interface ScheduledQualificationTemplateStore {
    fun find(code: String): ScheduledQualificationTemplate?
    fun save(item: ScheduledQualificationTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledQualificationTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledQualificationTemplateStore(
    var items: MutableMap<String, ScheduledQualificationTemplate>,
    var order: MutableList<String>,
) : ScheduledQualificationTemplateStore {
    override fun find(code: String): ScheduledQualificationTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledQualificationTemplate) {
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

    override fun list(): MutableList<ScheduledQualificationTemplate> {
        val out = mutableListOf<ScheduledQualificationTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledQualificationTemplateStore(): ScheduledQualificationTemplateStore {
    return MemoryScheduledQualificationTemplateStore(
        mutableMapOf<String, ScheduledQualificationTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledQualificationTemplateStore(
    var inner: ScheduledQualificationTemplateStore,
    var cache: TtlCache<ScheduledQualificationTemplate>,
    var hits: Int,
    var misses: Int,
) : ScheduledQualificationTemplateStore {
    override fun find(code: String): ScheduledQualificationTemplate? {
        val hit = (cache.get(code) as ScheduledQualificationTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledQualificationTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledQualificationTemplate> = inner.list()
}

fun newCachedScheduledQualificationTemplateStore(inner: ScheduledQualificationTemplateStore, ttl: Int, clock: Clock): ScheduledQualificationTemplateStore {
    return CachedScheduledQualificationTemplateStore(
        inner = inner,
        cache = newTtlCache<ScheduledQualificationTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled qualification templates. Empty fields match all. */
data class ScheduledQualificationTemplateQuery(
    var label: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledQualificationTemplate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledQualificationTemplate>): MutableList<ScheduledQualificationTemplate> {
        val matched = filter(items) { candidate: ScheduledQualificationTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledQualificationTemplates(store: ScheduledQualificationTemplateStore, query: ScheduledQualificationTemplateQuery): MutableList<ScheduledQualificationTemplate> {
    return query.apply(store.list())
}
