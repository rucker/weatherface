#include "pebble_os.h"
#include "pebble_app.h"
#include "pebble_fonts.h"


#define MY_UUID { 0xE2, 0x31, 0x01, 0xB0, 0xF4, 0x18, 0x4F, 0x72, 0x87, 0x09, 0xFC, 0x90, 0x79, 0x9F, 0xEE, 0x98 }
PBL_APP_INFO(MY_UUID,
             "Weatherface", "Rick Ucker",
             1, 0, /* App version */
             DEFAULT_MENU_ICON,
             APP_INFO_STANDARD_APP);

Window window;


void handle_init(AppContextRef ctx) {

  window_init(&window, "Main");
  window_set_background_color(&window, GColorBlack);
  window_stack_push(&window, true /* Animated */);
}


void pbl_main(void *params) {
  PebbleAppHandlers handlers = {
    .init_handler = &handle_init
  };
  app_event_loop(params, &handlers);
}
