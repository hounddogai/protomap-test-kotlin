package com.acme.transport.safety.coaching.assignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps global coaching templates by code. */
interface GlobalCoachingTemplateStore {
    fun find(code: String): GlobalCoachingTemplate?
    fun save(item: GlobalCoachingTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCoachingTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCoachingTemplateStore(
    var items: MutableMap<String, GlobalCoachingTemplate>,
    var order: MutableList<String>,
) : GlobalCoachingTemplateStore {
    override fun find(code: String): GlobalCoachingTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCoachingTemplate) {
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

    override fun list(): MutableList<GlobalCoachingTemplate> {
        val out = mutableListOf<GlobalCoachingTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCoachingTemplateStore(): GlobalCoachingTemplateStore {
    return MemoryGlobalCoachingTemplateStore(
        mutableMapOf<String, GlobalCoachingTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCoachingTemplateStore(
    var inner: GlobalCoachingTemplateStore,
    var cache: TtlCache<GlobalCoachingTemplate>,
    var hits: Int,
    var misses: Int,
) : GlobalCoachingTemplateStore {
    override fun find(code: String): GlobalCoachingTemplate? {
        val hit = (cache.get(code) as GlobalCoachingTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCoachingTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCoachingTemplate> = inner.list()
}

fun newCachedGlobalCoachingTemplateStore(inner: GlobalCoachingTemplateStore, ttl: Int, clock: Clock): GlobalCoachingTemplateStore {
    return CachedGlobalCoachingTemplateStore(
        inner = inner,
        cache = newTtlCache<GlobalCoachingTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global coaching templates. Empty fields match all. */
data class GlobalCoachingTemplateQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCoachingTemplate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalCoachingTemplate>): MutableList<GlobalCoachingTemplate> {
        val matched = filter(items) { candidate: GlobalCoachingTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCoachingTemplates(store: GlobalCoachingTemplateStore, query: GlobalCoachingTemplateQuery): MutableList<GlobalCoachingTemplate> {
    return query.apply(store.list())
}
