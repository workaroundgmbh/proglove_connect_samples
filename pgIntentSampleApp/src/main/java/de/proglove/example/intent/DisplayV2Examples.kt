package de.proglove.example.intent

import android.content.Intent

import de.proglove.example.common.ApiConstants.EXTRA_ORIENTATION
import de.proglove.example.common.ApiConstants.EXTRA_ACTION_BUTTONS
import de.proglove.example.common.ApiConstants.EXTRA_DISPLAY_ACTIVE_SCREEN_VIEW_ID
import de.proglove.example.common.ApiConstants.EXTRA_DISPLAY_SCREEN_ID
import de.proglove.example.common.ApiConstants.EXTRA_DISPLAY_SCREEN_TIMER
import de.proglove.example.common.ApiConstants.EXTRA_DISPLAY_SCREEN_VIEWS
import de.proglove.example.common.ApiConstants.EXTRA_REPLACE_QUEUE
import de.proglove.example.common.ApiConstants.ACTION_SET_SCREEN_V2_INTENT

/**
 * Intent to set a screen with the new Display V2 API.
 * This intent is used to demonstrate various screen configurations
 * and interactions available in the ProGlove SDK.
 *
 * Each object within this sealed class represents a different screen example,
 * with its own unique configuration and actions.
 *
 * For example:
 * - [PgNtfT5] demonstrates a notification screen with two buttons.
 * - [PgWork3Btn2T1] shows a screen with multiple fields and buttons.
 * - [PgListT1] illustrates a list view with multiple items.
 * - [TimerScreen] is a screen that automatically navigates back after a set time.
 */
sealed class DisplayV2Examples : Intent(ACTION_SET_SCREEN_V2_INTENT) {
    object PgNtfT5 : DisplayV2Examples() {
        init {
            putExtra(EXTRA_DISPLAY_SCREEN_ID, "pg_ntf_t5")
            putExtra(
                EXTRA_DISPLAY_SCREEN_VIEWS, """
                    [
                      {
                        "ref_id": "SCREEN_VIEW_1",
                        "pg_ntf_t5": {
                          "tagline": "Notification",
                          "message": "This is a message with two buttons",
                          "button_primary": {
                            "action_on_click": {
                              "basic": "NOTIFY"
                            },
                            "ref_id": "BUTTON_PRIMARY",
                            "text": "OK"
                          },
                          "button_secondary": {
                            "action_on_click": {
                              "basic": "NOTIFY"
                            },
                            "ref_id": "BUTTON_SECONDARY",
                            "text": "Cancel"
                          }
                        }
                      }
                    ]
                """.trimIndent()
            )
            putExtra(
                EXTRA_ACTION_BUTTONS, """
                    {
                      "front_outside": {
                        "ref_id": "ab1",
                        "text": "Notify",
                        "action_on_single_click": {
                          "basic": "NOTIFY"
                        },
                        "color": "YELLOW"
                      },
                      "back_outside": {
                        "ref_id": "ab2",
                        "text": "Back",
                        "color": "RED",
                        "action_on_single_click": {
                          "basic": "NAVIGATE_BACK"
                        }
                      },
                      "front_inside": {
                        "ref_id": "ab3",
                        "text": "Ok",
                        "color": "CYAN",
                        "action_on_single_click": {
                          "click_on_component": {
                            "ref_id": "BUTTON_PRIMARY"
                          }
                        }
                      },
                      "back_inside": {
                        "ref_id": "ab4",
                        "text": "Cancel",
                        "color": "GREEN",
                        "action_on_single_click": {
                          "click_on_component": {
                            "ref_id": "BUTTON_SECONDARY"
                          }
                        }
                      }
                    }
                """.trimIndent()
            )
            putExtra(EXTRA_REPLACE_QUEUE, true)
            putExtra(EXTRA_ORIENTATION, "LANDSCAPE")
        }
    }

    object PgWork3Btn2T1 : DisplayV2Examples() {
        init {
            putExtra(EXTRA_DISPLAY_ACTIVE_SCREEN_VIEW_ID, "SCREEN_VIEW_1")
            putExtra(EXTRA_DISPLAY_SCREEN_ID, "pg_work3_btn2_t1")
            putExtra(
                EXTRA_DISPLAY_SCREEN_VIEWS, """
                [
                  {
                    "ref_id": "SCREEN_VIEW_1",
                    "pg_work3_btn2_t1": {
                      "field_top": {
                        "ref_id": "field_top",
                        "text_content": "Top Content",
                        "text_header": "Top Header"
                      },
                      "field_middle_right": {
                        "ref_id": "field_middle_right",
                        "text_content": "Middle Right Content",
                        "text_header": "Middle Right Header",
                        "input_method": {
                          "num_pad": {}
                        }
                      },
                      "field_middle_left": {
                        "ref_id": "field_middle_left",
                        "text_content": "Middle Left Content",
                        "text_header": "Middle Left Header"
                      },
                      "button_1": {
                        "action_on_click": {
                          "basic": "NOTIFY"
                        },
                        "ref_id": "BUTTON_1",
                        "text": "Ok"
                      },
                      "button_2": {
                        "action_on_click": {
                          "basic": "NOTIFY"
                        },
                        "ref_id": "BUTTON_2",
                        "text": "Cancel"
                      }
                    }
                  },
                  {
                    "ref_id": "SCREEN_VIEW_2",
                    "pg_work2_t1": {
                      "field_top": {
                        "ref_id": "field_top",
                        "text_content": "Top Content",
                        "text_header": "Top Header"
                      },
                      "field_bottom": {
                        "ref_id": "field_bottom",
                        "text_content": "Bottom Content",
                        "text_header": "Bottom Header"
                      }
                    }
                  }
                ]
                """.trimIndent()
            )
            putExtra(
                EXTRA_ACTION_BUTTONS, """
                    {
                      "front_outside": {
                        "ref_id": "ab1",
                        "text": "Notify",
                        "action_on_single_click": {
                          "basic": "NOTIFY"
                        },
                        "color": "YELLOW"
                      },
                      "back_outside": {
                        "ref_id": "ab2",
                        "text": "Back",
                        "color": "RED",
                        "action_on_single_click": {
                          "basic": "NAVIGATE_BACK"
                        }
                      }
                    }
                """.trimIndent()
            )
            putExtra(EXTRA_REPLACE_QUEUE, true)
            putExtra(EXTRA_ORIENTATION, "PORTRAIT")
        }
    }

    object PgListT1 : DisplayV2Examples() {
        init {
            putExtra(EXTRA_DISPLAY_ACTIVE_SCREEN_VIEW_ID, "SCREEN_VIEW_1")
            putExtra(EXTRA_DISPLAY_SCREEN_ID, "pg_list_t1")
            putExtra(
                EXTRA_DISPLAY_SCREEN_VIEWS, """
                    [
                      {
                        "ref_id": "SCREEN_VIEW_1",
                        "pg_list_t1": {
                          "header": "List Header",
                          "items": [
                            {
                              "ref_id": "item_1",
                              "text_main": "Item 1",
                              "text_underline": "Description 1",
                              "text_trailing": "Trailing Text 1"
                            },
                            {
                              "ref_id": "item_2",
                              "text_main": "Item 2",
                              "text_underline": "Description 2",
                              "icon_trailing": "ARROW"
                            },
                            {
                              "ref_id": "item_3",
                              "text_main": "Item 3",
                              "text_underline": "Description 3"
                            },
                            {
                              "ref_id": "item_4",
                              "text_main": "Item 4",
                              "text_underline": "Description 4"
                            },
                            {
                              "ref_id": "item_5",
                              "text_main": "Item 5"
                            }
                          ]
                        }
                      }
                    ]
                """.trimIndent()
            )
            putExtra(
                EXTRA_ACTION_BUTTONS, """
                    {
                      "front_outside": {
                        "ref_id": "ab1",
                        "text": "Notify",
                        "action_on_single_click": {
                          "basic": "NOTIFY"
                        },
                        "color": "YELLOW"
                      },
                      "back_outside": {
                        "ref_id": "ab2",
                        "text": "Back",
                        "color": "RED",
                        "action_on_single_click": {
                          "basic": "NAVIGATE_BACK"
                        }
                      }
                    }
                """.trimIndent()
            )
            putExtra(EXTRA_REPLACE_QUEUE, true)
            putExtra(EXTRA_ORIENTATION, "PORTRAIT")
        }
    }

    object TimerScreen : DisplayV2Examples() {
        init {
            putExtra(EXTRA_DISPLAY_SCREEN_ID, "timer_screen")
            putExtra(
                EXTRA_DISPLAY_SCREEN_VIEWS, """
                    [
                      {
                        "ref_id": "SCREEN_VIEW_1",
                        "pg_work1_t1": {
                          "field_main": {
                            "text_header": "Timer Screen",
                            "text_content": "This screen will automatically navigate back after 2 seconds.",
                            "state": {
                              "type": "FOCUSED",
                              "highlighted": true
                            }
                          }
                        }
                      }
                    ]
                """.trimIndent()
            )
            putExtra(EXTRA_REPLACE_QUEUE, true)
            putExtra(EXTRA_ORIENTATION, "PORTRAIT")
            putExtra(EXTRA_DISPLAY_SCREEN_TIMER, """
                {
                  "timeout": 2000,
                  "action_on_expire": {
                    "basic": "NAVIGATE_BACK"
                  }
                }
                """.trimIndent()
            )
        }
    }
}
