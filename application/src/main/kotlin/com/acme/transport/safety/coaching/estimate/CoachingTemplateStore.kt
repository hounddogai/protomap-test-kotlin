package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching templates by code. */
interface CoachingTemplateStore {
    fun find(code: String): CoachingTemplate?
    fun save(item: CoachingTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingTemplateStore(
    var items: MutableMap<String, CoachingTemplate>,
    var order: MutableList<String>,
) : CoachingTemplateStore {
    override fun find(code: String): CoachingTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingTemplate) {
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

    override fun list(): MutableList<CoachingTemplate> {
        val out = mutableListOf<CoachingTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingTemplateStore(): CoachingTemplateStore {
    return MemoryCoachingTemplateStore(mutableMapOf<String, CoachingTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingTemplateStore(
    var inner: CoachingTemplateStore,
    var cache: TtlCache<CoachingTemplate>,
    var hits: Int,
    var misses: Int,
) : CoachingTemplateStore {
    override fun find(code: String): CoachingTemplate? {
        val hit = (cache.get(code) as CoachingTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingTemplate> = inner.list()
}

fun newCachedCoachingTemplateStore(inner: CoachingTemplateStore, ttl: Int, clock: Clock): CoachingTemplateStore {
    return CachedCoachingTemplateStore(
        inner = inner,
        cache = newTtlCache<CoachingTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching templates. Empty fields match all. */
data class CoachingTemplateQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingTemplate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingTemplate>): MutableList<CoachingTemplate> {
        val matched = filter(items) { candidate: CoachingTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingTemplates(store: CoachingTemplateStore, query: CoachingTemplateQuery): MutableList<CoachingTemplate> {
    return query.apply(store.list())
}
