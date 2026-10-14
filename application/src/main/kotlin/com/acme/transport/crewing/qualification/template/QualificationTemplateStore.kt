package com.acme.transport.crewing.qualification.template

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification templates by code. */
interface QualificationTemplateStore {
    fun find(code: String): QualificationTemplate?
    fun save(item: QualificationTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationTemplateStore(
    var items: MutableMap<String, QualificationTemplate>,
    var order: MutableList<String>,
) : QualificationTemplateStore {
    override fun find(code: String): QualificationTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationTemplate) {
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

    override fun list(): MutableList<QualificationTemplate> {
        val out = mutableListOf<QualificationTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationTemplateStore(): QualificationTemplateStore {
    return MemoryQualificationTemplateStore(
        mutableMapOf<String, QualificationTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationTemplateStore(
    var inner: QualificationTemplateStore,
    var cache: TtlCache<QualificationTemplate>,
    var hits: Int,
    var misses: Int,
) : QualificationTemplateStore {
    override fun find(code: String): QualificationTemplate? {
        val hit = (cache.get(code) as QualificationTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationTemplate> = inner.list()
}

fun newCachedQualificationTemplateStore(inner: QualificationTemplateStore, ttl: Int, clock: Clock): QualificationTemplateStore {
    return CachedQualificationTemplateStore(
        inner = inner,
        cache = newTtlCache<QualificationTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification templates. Empty fields match all. */
data class QualificationTemplateQuery(
    var unit: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationTemplate): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationTemplate>): MutableList<QualificationTemplate> {
        val matched = filter(items) { candidate: QualificationTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationTemplates(store: QualificationTemplateStore, query: QualificationTemplateQuery): MutableList<QualificationTemplate> {
    return query.apply(store.list())
}
